"""
services/completion_service.py
──────────────────────────────
Phase 7.2 Service layer for Completion Time Estimation.
Handles model inference, generating confidence ranges, and reasonings.
"""

from typing import Dict, Any
import logging
from api.schemas.request import CompletionTimeRequest
from api.schemas.response import CompletionTimeResponse, ConfidenceRange, FallbackCompletionResponse
from models import loader

logger = logging.getLogger(__name__)

def predict_completion_time(request: CompletionTimeRequest) -> CompletionTimeResponse:
    """
    Executes completion time prediction using the loaded gradient boosting model.
    """
    model_bundle = loader.get_model("completion")
    
    if not model_bundle:
        logger.error("[CompletionService] Model 'completion' not found. Returning fallback.")
        fallback_val = request.manual_estimate if request.manual_estimate else 0.0
        return FallbackCompletionResponse(
            estimated_hours=fallback_val,
            confidence_range=ConfidenceRange(low=fallback_val, high=fallback_val),
            reasoning="Model unavailable — returning manual estimate or 0.0",
            fallback_used=True
        )

    pipeline = model_bundle["pipeline"]
    
    # 1. Build DataFrame exactly matching training features
    # Required features: priority, title_length, description_length, emp_on_time_rate, 
    # emp_avg_hours_by_priority, emp_active_tasks
    import pandas as pd
    
    # Determine which avg hours to use based on priority
    if request.priority in ["CRITICAL", "HIGH"]:
        avg_hours = request.emp_avg_hours_high
    else:
        avg_hours = request.emp_avg_hours_medium

    input_data = pd.DataFrame([{
        "priority": request.priority,
        "title_length": len(request.task_title),
        "description_length": len(request.task_description),
        "emp_on_time_rate": request.emp_on_time_rate,
        "emp_avg_hours_by_priority": avg_hours,
        "emp_active_tasks": request.emp_active_tasks
    }])

    try:
        # 2. Predict
        predicted_hours = float(pipeline.predict(input_data)[0])
        # Ensure it doesn't predict less than 30 mins
        predicted_hours = max(0.5, round(predicted_hours, 1))
        
        # 3. Confidence Range (heuristic based on priority variance and MAE)
        # Using a fixed ±15% range for MVP, bound to +/- 0.5 minimum
        margin = max(0.5, predicted_hours * 0.15)
        low_bound = max(0.5, round(predicted_hours - margin, 1))
        high_bound = round(predicted_hours + margin, 1)

        # 4. Generate Reasoning string
        reasoning = (
            f"{request.priority} priority tasks for this employee usually take ~{round(avg_hours, 1)} hrs. "
            f"Active tasks: {request.emp_active_tasks}. On-time rate: {int(request.emp_on_time_rate * 100)}%."
        )

        logger.info(f"[CompletionService] Predicted {predicted_hours} hrs for '{request.task_title}'.")

        return CompletionTimeResponse(
            estimated_hours=predicted_hours,
            confidence_range=ConfidenceRange(low=low_bound, high=high_bound),
            reasoning=reasoning,
            fallback_used=False
        )
        
    except Exception as e:
        logger.error(f"[CompletionService] Error during prediction: {str(e)}", exc_info=True)
        fallback_val = request.manual_estimate if request.manual_estimate else 0.0
        return FallbackCompletionResponse(
            estimated_hours=fallback_val,
            confidence_range=ConfidenceRange(low=fallback_val, high=fallback_val),
            reasoning=f"Error during prediction: {str(e)}",
            fallback_used=True
        )
