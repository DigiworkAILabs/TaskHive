"""
services/priority_service.py
─────────────────────────────
Phase 7.1 — Task Priority Suggestion
Business logic for priority inference.

Flow:
    1. Receive TaskPriorityRequest
    2. Build feature row via feature_engineering
    3. Run pipeline.predict() + predict_proba()
    4. Decode label + compute confidence
    5. Generate reasoning string
    6. Return TaskPriorityResponse (or fallback on error)
"""

import logging
from typing import Optional

from api.schemas.request import TaskPriorityRequest
from api.schemas.response import TaskPriorityResponse, FallbackPriorityResponse, PriorityLabel
from models.loader import get_model
from preprocessing.feature_engineering import (
    build_priority_feature_row,
    get_confidence,
    generate_reasoning,
)

logger = logging.getLogger(__name__)


def predict_priority(request: TaskPriorityRequest) -> TaskPriorityResponse:
    """
    Run priority inference for the given task request.

    Returns TaskPriorityResponse with predicted label, confidence,
    and reasoning. Falls back to MEDIUM if model is unavailable.
    """

    # 1. Load model bundle (loaded once at startup by loader.py)
    bundle = get_model("priority")
    if bundle is None:
        logger.warning("[PriorityService] Model not loaded — returning fallback MEDIUM")
        return FallbackPriorityResponse()

    try:
        pipeline     = bundle["pipeline"]
        le           = bundle["label_encoder"]
        label_order  = bundle["label_order"]

        # 2. Build feature DataFrame row
        feature_df = build_priority_feature_row(
            task_title          = request.task_title,
            task_description    = request.task_description,
            tags                = request.tags,
            estimated_hours     = request.estimated_hours or 0.0,
            emp_completion_rate = request.emp_completion_rate or 0.75,
            emp_avg_hours       = request.emp_avg_hours or 4.0,
        )

        # 3. Predict label + probabilities
        pred_encoded   = pipeline.predict(feature_df)[0]          # int
        proba_array    = pipeline.predict_proba(feature_df)[0]     # array of class probabilities

        # 4. Decode label
        predicted_label: str = label_order[int(pred_encoded)]
        confidence: float    = get_confidence(proba_array)

        # 5. Generate reasoning
        reasoning = generate_reasoning(
            predicted_priority = predicted_label,
            task_title         = request.task_title,
            task_description   = request.task_description,
            tags               = request.tags,
            confidence         = confidence,
        )

        logger.info(
            "[PriorityService] Predicted %s (confidence=%.2f) for title='%s'",
            predicted_label, confidence, request.task_title[:60]
        )

        return TaskPriorityResponse(
            predicted_priority = PriorityLabel(predicted_label),
            confidence         = confidence,
            reasoning          = reasoning,
            fallback_used      = False,
        )

    except Exception as exc:
        logger.error("[PriorityService] Inference error: %s", exc, exc_info=True)
        return FallbackPriorityResponse()
