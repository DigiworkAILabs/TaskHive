"""
api/routes/predict_priority.py
───────────────────────────────
Phase 7.1 — Task Priority Suggestion
FastAPI route: POST /ml/predict/task-priority
"""

import logging
from fastapi import APIRouter, HTTPException, status

from api.schemas.request import TaskPriorityRequest
from api.schemas.response import TaskPriorityResponse
from services.priority_service import predict_priority

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/ml",
    tags=["Priority Prediction"],
)


@router.post(
    "/predict/task-priority",
    response_model=TaskPriorityResponse,
    summary="Predict Task Priority",
    description=(
        "Accepts task title, description, tags, estimated hours, and assignee "
        "performance metrics. Returns a predicted priority (LOW / MEDIUM / HIGH / CRITICAL) "
        "with a confidence score and human-readable reasoning. "
        "Always returns a response — falls back to MEDIUM if the model is unavailable."
    ),
    status_code=status.HTTP_200_OK,
)
async def predict_task_priority(request: TaskPriorityRequest) -> TaskPriorityResponse:
    """
    ML inference endpoint for task priority suggestion.

    Called by Spring Boot's MLClientService.
    Never raises an error — graceful fallback is guaranteed.
    """
    logger.info(
        "[PriorityRoute] Received request: title='%s' tags=%s estimated_hours=%s",
        request.task_title[:60],
        request.tags,
        request.estimated_hours,
    )

    result = predict_priority(request)

    logger.info(
        "[PriorityRoute] Returning: priority=%s confidence=%.2f fallback=%s",
        result.predicted_priority,
        result.confidence,
        result.fallback_used,
    )

    return result
