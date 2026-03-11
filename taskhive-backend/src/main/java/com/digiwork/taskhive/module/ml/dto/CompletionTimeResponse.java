package com.digiwork.taskhive.module.ml.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CompletionTimeResponse
 * ──────────────────────
 * DTO returned to the Next.js frontend from POST
 * /api/v1/ml/predict/completion-time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompletionTimeResponse {

    /** Predicted completion time in hours */
    private Double estimatedHours;

    /** Confidence range from the ML model */
    private ConfidenceRange confidenceRange;

    /** Human-readable reasoning */
    private String reasoning;

    /** True when ML server was down and manual estimate or default was returned */
    private boolean fallbackUsed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceRange {
        private Double low;
        private Double high;
    }

    // ── Static factory methods ────────────────────────────────────────────────

    /** Build a fallback response per SRS specification */
    public static CompletionTimeResponse fallback(Double manualEstimate) {
        Double fallbackVal = (manualEstimate != null) ? manualEstimate : 0.0;
        return CompletionTimeResponse.builder()
                .estimatedHours(fallbackVal)
                .confidenceRange(new ConfidenceRange(fallbackVal, fallbackVal))
                .reasoning("ML model unavailable — returning manual estimate or 0.0.")
                .fallbackUsed(true)
                .build();
    }

    /** Convenience builder from FastAPI response */
    public static CompletionTimeResponse of(Double hours, Double low, Double high, String reasoning) {
        return CompletionTimeResponse.builder()
                .estimatedHours(hours)
                .confidenceRange(new ConfidenceRange(low, high))
                .reasoning(reasoning)
                .fallbackUsed(false)
                .build();
    }
}
