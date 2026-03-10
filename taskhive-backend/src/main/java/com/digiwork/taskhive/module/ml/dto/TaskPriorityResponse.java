package com.digiwork.taskhive.module.ml.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TaskPriorityResponse
 * ─────────────────────
 * DTO returned to the Next.js frontend from POST
 * /api/v1/ml/predict/task-priority.
 *
 * Wraps the FastAPI response with an additional fallbackUsed flag so the
 * frontend can know whether the result is a real ML prediction or a default.
 *
 * SRS fallback: { predictedPriority: "MEDIUM", confidence: 0.5, fallbackUsed:
 * true }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskPriorityResponse {

    /** Predicted priority: LOW / MEDIUM / HIGH / CRITICAL */
    private String predictedPriority;

    /** Model confidence score between 0.0 and 1.0 */
    private Double confidence;

    /** Human-readable reasoning for the prediction */
    private String reasoning;

    /** True when ML server was down and a default was returned */
    private boolean fallbackUsed;

    // ── Static factory methods ────────────────────────────────────────────────

    /** Build a fallback response per SRS specification */
    public static TaskPriorityResponse fallback() {
        return TaskPriorityResponse.builder()
                .predictedPriority("MEDIUM")
                .confidence(0.5)
                .reasoning("ML model unavailable — default priority MEDIUM returned.")
                .fallbackUsed(true)
                .build();
    }

    /** Convenience builder from FastAPI response fields */
    public static TaskPriorityResponse of(String priority, double confidence, String reasoning) {
        return TaskPriorityResponse.builder()
                .predictedPriority(priority)
                .confidence(confidence)
                .reasoning(reasoning)
                .fallbackUsed(false)
                .build();
    }
}
