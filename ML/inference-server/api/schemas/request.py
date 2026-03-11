"""
api/schemas/request.py
───────────────────────
Pydantic request schemas for the ML Inference Server.
Phase 7.1: TaskPriorityRequest
Phase 7.2: CompletionTimeRequest
Phase 7.3: WorkloadBalanceRequest
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


class CandidateEmployee(BaseModel):
    """Individual candidate employee with performance stats."""

    employee_id: str = Field(
        ...,
        description="UUID of the candidate employee",
        examples=["550e8400-e29b-41d4-a716-446655440000"],
    )

    active_tasks: int = Field(
        ...,
        ge=0,
        le=100,
        description="Number of currently assigned active tasks",
        examples=[3],
    )

    completion_rate: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Historical task completion rate (0.0–1.0)",
        examples=[0.91],
    )

    on_time_rate: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Historical on-time delivery rate (0.0–1.0)",
        examples=[0.85],
    )

    avg_hours_per_task: float = Field(
        default=4.0,
        ge=0.0,
        le=24.0,
        description="Average hours per task for this employee",
        examples=[4.5],
    )

    dept_match: bool = Field(
        ...,
        description="True if employee's department matches the task",
        examples=[True],
    )


class WorkloadBalanceRequest(BaseModel):
    """
    Incoming request body for POST /ml/recommend/workload-balance.
    """

    task_priority: str = Field(
        ...,
        description="Task priority: LOW / MEDIUM / HIGH / CRITICAL",
        examples=["HIGH"],
    )

    task_estimated_hours: float = Field(
        default=4.0,
        ge=0.0,
        le=500.0,
        description="Estimated hours for this task",
        examples=[8.0],
    )

    candidates: List[CandidateEmployee] = Field(
        ...,
        min_length=1,
        description="List of candidate employees with their performance stats",
    )


class CompletionTimeRequest(BaseModel):
    """
    Incoming request body for POST /ml/predict/completion-time.
    Sent by Spring Boot after enriching with assignee stats.
    """

    task_title: str = Field(..., min_length=2, max_length=500)
    task_description: str = Field(default="", max_length=5000)
    priority: str = Field(
        ...,
        pattern="^(LOW|MEDIUM|HIGH|CRITICAL)$",
        description="Task priority"
    )

    # Employee metrics enriched by Spring Boot
    emp_on_time_rate: float = Field(
        default=0.75,
        ge=0.0,
        le=1.0,
        description="Assignee historical on-time task completion rate (0.0–1.0)"
    )

    emp_avg_hours_high: float = Field(
        default=5.0,
        ge=0.0,
        description="Assignee average hours per HIGH/CRITICAL priority task"
    )
    
    emp_avg_hours_medium: float = Field(
        default=3.0,
        ge=0.0,
        description="Assignee average hours per MEDIUM/LOW priority task"
    )

    emp_active_tasks: int = Field(
        default=0,
        ge=0,
        description="Number of currently active tasks for this assignee"
    )

    manual_estimate: Optional[float] = Field(
        default=None,
        ge=0.0,
        description="Admin's manually entered estimate (if any)"
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

    model_config = {
        "json_schema_extra": {
            "example": {
                "task_title": "Build dashboard charts",
                "task_description": "Add pie and bar charts to analytics page",
                "priority": "HIGH",
                "emp_on_time_rate": 0.78,
                "emp_avg_hours_high": 6.2,
                "emp_avg_hours_medium": 3.8,
                "emp_active_tasks": 3,
                "manual_estimate": 5.0
            }
        }
    }
