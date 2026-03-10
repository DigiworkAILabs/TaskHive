"""
api/schemas/request.py
───────────────────────
Pydantic request schemas for the ML Inference Server.
Phase 7.1: TaskPriorityRequest only.
"""

from pydantic import BaseModel, Field, field_validator
from typing import List, Optional


class TaskPriorityRequest(BaseModel):
    """
    Incoming request body for POST /ml/predict/task-priority.

    Sent by Spring Boot after enriching the frontend payload
    with employee performance metrics.
    """

    task_title: str = Field(
        ...,
        min_length=2,
        max_length=500,
        description="Task title provided by the admin",
        examples=["Fix login page crash"],
    )

    task_description: str = Field(
        default="",
        max_length=5000,
        description="Optional task description",
        examples=["Users are unable to login on mobile after the last release."],
    )

    tags: List[str] = Field(
        default_factory=list,
        description="List of task tags",
        examples=[["bug", "frontend"]],
    )

    estimated_hours: Optional[float] = Field(
        default=None,
        ge=0.0,
        le=500.0,
        description="Estimated hours to complete the task",
        examples=[3.0],
    )

    # Employee performance features — enriched by Spring Boot
    emp_completion_rate: Optional[float] = Field(
        default=0.75,
        ge=0.0,
        le=1.0,
        description="Assignee historical task completion rate (0.0–1.0)",
        examples=[0.87],
    )

    emp_avg_hours: Optional[float] = Field(
        default=4.0,
        ge=0.0,
        le=24.0,
        description="Assignee average hours per task",
        examples=[4.2],
    )

    department: Optional[str] = Field(
        default=None,
        max_length=100,
        description="Assignee's department (informational only)",
        examples=["Engineering"],
    )

    @field_validator("task_title")
    @classmethod
    def title_not_blank(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("task_title must not be blank")
        return v.strip()

    @field_validator("task_description")
    @classmethod
    def strip_description(cls, v: str) -> str:
        return v.strip() if v else ""

    @field_validator("tags")
    @classmethod
    def clean_tags(cls, v: List[str]) -> List[str]:
        return [t.strip().lower() for t in v if t.strip()]

    model_config = {
        "json_schema_extra": {
            "example": {
                "task_title": "Fix login page crash",
                "task_description": "Users are unable to login on mobile after the last release.",
                "tags": ["bug", "frontend"],
                "estimated_hours": 3.0,
                "emp_completion_rate": 0.87,
                "emp_avg_hours": 4.2,
                "department": "Engineering",
            }
        }
    }
