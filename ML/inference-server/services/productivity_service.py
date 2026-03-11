import pandas as pd
import numpy as np
from typing import Optional
from api.schemas.request import ProductivityScoreRequest
from api.schemas.response import ProductivityScoreResponse, ScoreBreakdown, FallbackProductivityResponse
from models.loader import load_productivity_model

class ProductivityService:
    def __init__(self):
        self.model_data = load_productivity_model()
        self.pipeline = self.model_data.get("pipeline") if self.model_data else None

    def get_score(self, request: ProductivityScoreRequest) -> ProductivityScoreResponse:
        if not self.pipeline:
            return self._get_fallback(request)

        try:
            # 1. Feature Engineering (match training script)
            completion_rate = request.tasks_completed / request.tasks_assigned if request.tasks_assigned > 0 else 0.0
            overdue_rate = request.tasks_overdue / request.tasks_assigned if request.tasks_assigned > 0 else 0.0
            engagement_normalized = min(request.comment_activity / 50.0, 1.0)

            features = pd.DataFrame([{
                'completion_rate': completion_rate,
                'on_time_rate': request.on_time_rate,
                'overdue_rate': overdue_rate,
                'engagement_normalized': engagement_normalized,
                'avg_completion_hours': request.avg_completion_hours
            }])

            # 2. Predict Score
            score = float(self.pipeline.predict(features)[0])
            score = max(0.0, min(100.0, score))

            # 3. Calculate Grade
            grade = self._calculate_grade(score)

            # 4. Calculate Breakdown for UI (using SRS weights)
            breakdown = ScoreBreakdown(
                completion_rate_score=round(completion_rate * 35, 1),
                on_time_score=round(request.on_time_rate * 30, 1),
                overdue_penalty=round(-(overdue_rate * 20), 1),
                engagement_score=round(engagement_normalized * 15, 1)
            )

            # 5. Determine Trend
            trend = "stable"
            if request.prev_score is not None:
                diff = score - request.prev_score
                if diff > 2.0:
                    trend = "improving"
                elif diff < -2.0:
                    trend = "declining"

            # 6. Generate Reasoning
            reasoning = self._generate_reasoning(request, score, grade, trend)

            return ProductivityScoreResponse(
                score=round(score, 1),
                grade=grade,
                breakdown=breakdown,
                trend=trend,
                reasoning=reasoning,
                fallback_used=False
            )

        except Exception as e:
            print(f"[ProductivityService] Prediction error: {e}")
            return self._get_fallback(request)

    def _calculate_grade(self, score: float) -> str:
        if score >= 90: return "A"
        if score >= 75: return "B"
        if score >= 60: return "C"
        if score >= 45: return "D"
        return "F"

    def _generate_reasoning(self, req, score, grade, trend) -> str:
        reasons = []
        if req.on_time_rate > 0.85:
            reasons.append("Excellent on-time delivery.")
        elif req.on_time_rate < 0.6:
            reasons.append("On-time delivery needs improvement.")

        if (req.tasks_completed / req.tasks_assigned if req.tasks_assigned > 0 else 0) > 0.9:
            reasons.append("High task completion rate.")
        
        if req.tasks_overdue > 2:
            reasons.append("Multiple overdue tasks impacting score.")

        if trend == "improving":
            reasons.append("Performance is trending upwards.")
        
        if not reasons:
            reasons.append("Consistent performance across metrics.")

        return " ".join(reasons)

    def _get_fallback(self, request: ProductivityScoreRequest) -> FallbackProductivityResponse:
        return FallbackProductivityResponse()

# Singleton instance
productivity_service = ProductivityService()
