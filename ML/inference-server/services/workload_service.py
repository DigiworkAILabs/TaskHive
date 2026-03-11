"""
services/workload_service.py
──────────────────────────────
Phase 7.3 — Workload Balance Recommendation
Business logic for workload balance inference.

Flow:
    1. Receive WorkloadBalanceRequest (list of candidates)
    2. Build feature rows for each candidate
    3. Run pipeline.predict() for each candidate to get scores
    4. Rank candidates by score descending
    5. Generate reasoning string
    6. Return WorkloadBalanceResponse (or fallback on error)
"""

import logging
import numpy as np
import pandas as pd
from typing import List

from api.schemas.request import WorkloadBalanceRequest, CandidateEmployee
from api.schemas.response import (
    WorkloadBalanceResponse,
    FallbackWorkloadResponse,
    EmployeeScore,
)
from models.loader import get_model

logger = logging.getLogger(__name__)

# Priority encoding map (must match training data)
PRIORITY_MAP = {
    "LOW": 1,
    "MEDIUM": 2,
    "HIGH": 3,
    "CRITICAL": 4,
}


def _build_features(candidate: CandidateEmployee, task_priority_encoded: int) -> dict:
    """Build a feature dict for a single candidate matching training columns."""
    return {
        "active_tasks": candidate.active_tasks,
        "completion_rate": candidate.completion_rate,
        "on_time_rate": candidate.on_time_rate,
        "dept_match": 1 if candidate.dept_match else 0,
        "task_priority_encoded": task_priority_encoded,
    }


def _generate_reasoning(
    best: CandidateEmployee,
    best_score: float,
) -> str:
    """Produce a human-readable reasoning string for the recommendation."""
    parts = [
        f"{best.employee_id} has",
        f"low active tasks ({best.active_tasks})" if best.active_tasks <= 3 else f"active tasks ({best.active_tasks})",
        f"and high completion rate ({best.completion_rate:.0%})" if best.completion_rate >= 0.80 else f"and completion rate ({best.completion_rate:.0%})",
    ]
    reasoning = " ".join(parts) + f". Score: {best_score:.1f}/100."
    return reasoning


def recommend_workload(request: WorkloadBalanceRequest) -> WorkloadBalanceResponse:
    """
    Run workload balance inference for all candidates.

    Returns WorkloadBalanceResponse with recommended employee, score breakdown,
    and reasoning. Falls back to null recommendation if model is unavailable.
    """

    # 1. Load model bundle
    bundle = get_model("workload")
    if bundle is None:
        logger.warning("[WorkloadService] Model not loaded — returning fallback")
        return FallbackWorkloadResponse()

    try:
        pipeline = bundle["pipeline"]
        feature_cols = bundle["feature_cols"]

        # 2. Encode task priority
        task_priority_encoded = PRIORITY_MAP.get(
            request.task_priority.upper(), 2  # Default to MEDIUM
        )

        # 3. Build feature DataFrame for all candidates
        rows = []
        for candidate in request.candidates:
            rows.append(_build_features(candidate, task_priority_encoded))

        df = pd.DataFrame(rows, columns=feature_cols)

        # 4. Predict scores for all candidates
        raw_scores = pipeline.predict(df)

        # Clamp scores to [0, 100]
        scores = np.clip(raw_scores, 0.0, 100.0)

        # 5. Build score breakdown (sorted descending)
        employee_scores: List[EmployeeScore] = []
        for i, candidate in enumerate(request.candidates):
            employee_scores.append(
                EmployeeScore(
                    employee_id=candidate.employee_id,
                    score=round(float(scores[i]), 1),
                )
            )

        # Sort by score descending
        employee_scores.sort(key=lambda x: x.score, reverse=True)

        # 6. Best candidate
        best_employee = employee_scores[0]
        best_idx = next(
            i for i, c in enumerate(request.candidates)
            if c.employee_id == best_employee.employee_id
        )
        best_candidate = request.candidates[best_idx]

        # 7. Generate reasoning
        reasoning = _generate_reasoning(best_candidate, best_employee.score)

        logger.info(
            "[WorkloadService] Recommended %s (score=%.1f) from %d candidates",
            best_employee.employee_id,
            best_employee.score,
            len(request.candidates),
        )

        return WorkloadBalanceResponse(
            recommended_employee_id=best_employee.employee_id,
            score_breakdown=employee_scores,
            reasoning=reasoning,
            fallback_used=False,
        )

    except Exception as exc:
        logger.error("[WorkloadService] Inference error: %s", exc, exc_info=True)
        return FallbackWorkloadResponse()
