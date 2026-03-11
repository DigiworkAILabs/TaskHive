"""
api/routes/recommend_workload.py
─────────────────────────────────
Phase 7.3 — Workload Balance Recommendation
FastAPI route: POST /ml/recommend/workload-balance
"""

import logging
from fastapi import APIRouter, status

from api.schemas.request import WorkloadBalanceRequest
from api.schemas.response import WorkloadBalanceResponse
from services.workload_service import recommend_workload

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/ml",
    tags=["Workload Balance"],
)


@router.post(
    "/recommend/workload-balance",
    response_model=WorkloadBalanceResponse,
    summary="Recommend Best Employee for Task Assignment",
    description=(
        "Accepts task priority, estimated hours, and a list of candidate employees "
        "with their performance stats. Returns the recommended employee ID, "
        "score breakdown for all candidates, and human-readable reasoning. "
        "Always returns a response — falls back to null recommendation if the model is unavailable."
    ),
    status_code=status.HTTP_200_OK,
)
async def recommend_workload_balance(
    request: WorkloadBalanceRequest,
) -> WorkloadBalanceResponse:
    """
    ML inference endpoint for workload balance recommendation.

    Called by Spring Boot's MLClientService.
    Never raises an error — graceful fallback is guaranteed.
    """
    logger.info(
        "[WorkloadRoute] Received request: priority='%s' candidates=%d",
        request.task_priority,
        len(request.candidates),
    )

    result = recommend_workload(request)

    logger.info(
        "[WorkloadRoute] Returning: recommended=%s fallback=%s",
        result.recommended_employee_id,
        result.fallback_used,
    )

    return result
