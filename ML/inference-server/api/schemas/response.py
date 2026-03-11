"""
api/schemas/response.py
────────────────────────
Pydantic response schemas for the ML Inference Server.
Phase 7.1: TaskPriorityResponse
Phase 7.2: CompletionTimeResponse
Phase 7.3: WorkloadBalanceResponse
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from enum import Enum


class PriorityLabel(str, Enum):
    LOW      = "LOW"
    MEDIUM   = "MEDIUM"
    HIGH     = "HIGH"
    CRITICAL = "CRITICAL"


class TaskPriorityResponse(BaseModel):
    """
    Response body for POST /ml/predict/task-priority.
    Spring Boot wraps this in its standard ApiResponse envelope.
    """

    predicted_priority: PriorityLabel = Field(
        ...,
        description="ML-predicted task priority label",
        examples=["HIGH"],
    )

    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Model confidence score (0.0 = no confidence, 1.0 = certain)",
        examples=[0.84],
    )

    reasoning: str = Field(
        ...,
        description="Human-readable explanation of the prediction",
        examples=["Bug-related task with mobile impact detected. Confidence: 84%."],
    )

    fallback_used: bool = Field(
        default=False,
        description="True when ML model was unavailable and a default was returned",
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "predicted_priority": "HIGH",
                "confidence": 0.84,
                "reasoning": "Bug-related task with mobile impact detected. Confidence: 84%.",
                "fallback_used": False,
            }
        }
    }


class FallbackPriorityResponse(TaskPriorityResponse):
    """
    Used when the model is unavailable (model not loaded).
    Returns MEDIUM with 0.5 confidence per SRS specification.
    """
    predicted_priority: PriorityLabel = PriorityLabel.MEDIUM
    confidence: float = 0.5
    reasoning: str = "ML model unavailable — default priority MEDIUM returned."
    fallback_used: bool = True


# ── Phase 7.2: Task Completion Time Estimation ────────────────────────────────

class ConfidenceRange(BaseModel):
    low: float
    high: float


class CompletionTimeResponse(BaseModel):
    """
    Response body for POST /ml/predict/completion-time.
    """

    estimated_hours: float = Field(
        ...,
        ge=0.0,
        description="Predicted hours to complete the task"
    )

    confidence_range: ConfidenceRange = Field(
        ...,
        description="Range of estimated hours (low/high bounds)"
    )

    reasoning: str = Field(
        ...,
        description="Human-readable explanation of the prediction"
    )

    fallback_used: bool = Field(
        default=False,
        description="True when ML model was unavailable"
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "estimated_hours": 5.5,
                "confidence_range": {"low": 4.0, "high": 7.0},
                "reasoning": "HIGH priority tasks for this employee avg 6.2hrs",
                "fallback_used": False
            }
        }
    }


class FallbackCompletionResponse(CompletionTimeResponse):
    """
    Used when the model is unavailable (model not loaded).
    Returns admin's manual estimate or 0.0 with fallback flag.
    """
    estimated_hours: float = 0.0
    confidence_range: ConfidenceRange = ConfidenceRange(low=0.0, high=0.0)
    reasoning: str = "ML model unavailable — manual estimate or default 0.0 returned."
    fallback_used: bool = True


# ── Phase 7.3: Workload Balance Recommendation ────────────────────────────────

class EmployeeScore(BaseModel):
    """Score breakdown for a single candidate employee."""

    employee_id: str = Field(
        ...,
        description="UUID of the candidate employee",
        examples=["550e8400-e29b-41d4-a716-446655440000"],
    )

    score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Suitability score (0–100)",
        examples=[87.4],
    )


class WorkloadBalanceResponse(BaseModel):
    """
    Response body for POST /ml/recommend/workload-balance.
    Spring Boot wraps this in its standard ApiResponse envelope.
    """

    recommended_employee_id: Optional[str] = Field(
        ...,
        description="UUID of the best-fit employee, or null on fallback",
        examples=["550e8400-e29b-41d4-a716-446655440000"],
    )

    score_breakdown: List[EmployeeScore] = Field(
        default_factory=list,
        description="All candidates ranked by score descending",
    )

    reasoning: str = Field(
        ...,
        description="Human-readable explanation of the recommendation",
        examples=["uuid1 has low active tasks (2) and high completion rate (91%)"],
    )

    fallback_used: bool = Field(
        default=False,
        description="True when ML model was unavailable and a default was returned",
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "recommended_employee_id": "uuid1",
                "score_breakdown": [
                    {"employee_id": "uuid1", "score": 87.4},
                    {"employee_id": "uuid2", "score": 52.1},
                ],
                "reasoning": "uuid1 has low active tasks (2) and high completion rate (91%)",
                "fallback_used": False,
            }
        }
    }


class FallbackWorkloadResponse(WorkloadBalanceResponse):
    """
    Used when the workload model is unavailable.
    Returns null recommendation per SRS specification.
    """

    recommended_employee_id: Optional[str] = None
    score_breakdown: List[EmployeeScore] = []
    reasoning: str = "Please select manually"
    fallback_used: bool = True
