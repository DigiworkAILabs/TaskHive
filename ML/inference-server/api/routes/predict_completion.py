"""
api/routes/predict_completion.py
────────────────────────────────
Phase 7.2 FastAPI router for POST /ml/predict/completion-time.
Receives incoming payloads, passes to the service layer.
"""

from fastapi import APIRouter
from api.schemas.request import CompletionTimeRequest
from api.schemas.response import CompletionTimeResponse
from services.completion_service import predict_completion_time
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/ml", tags=["Completion Time Prediction"])

@router.post("/predict/completion-time", response_model=CompletionTimeResponse)
async def predict_completion_endpoint(request: CompletionTimeRequest):
    """
    Predict how many hours a task will take to complete.
    
    Receives:
      - task limits (title, description, priority)
      - employee metrics (historical rates, active tasks)
      
    Returns:
      - Estimated hours (float)
      - Confidence range dict (low bounds, high bounds)
      - Reasoning string
    """
    logger.info(f"[Route] POST /completion-time for task: '{request.task_title[:30]}...'")
    
    # Pass to service layer
    response = predict_completion_time(request)
    return response
