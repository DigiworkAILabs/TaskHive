package com.digiwork.taskhive.module.ml.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WorkloadRecommendationResponse
 * ────────────────────────────────
 * DTO returned to the Next.js frontend from POST
 * /api/v1/ml/recommend/workload-balance.
 *
 * Wraps the FastAPI response with an additional fallbackUsed flag so the
 * frontend can distinguish a real ML recommendation from a default.
 *
 * SRS fallback: { recommendedEmployeeId: null, reasoning: "Please select
 * manually", fallbackUsed: true }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkloadRecommendationResponse {

    /** UUID of the recommended employee, or null on fallback */
    private String recommendedEmployeeId;

    /** Score breakdown for all candidates, sorted by score descending */
    private List<EmployeeScoreBreakdown> scoreBreakdown;

    /** Human-readable reasoning for the recommendation */
    private String reasoning;

    /** True when ML server was down and a default was returned */
    private boolean fallbackUsed;

    // ── Inner class: score per employee
    // ───────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeScoreBreakdown {
        private String employeeId;
        private Double score;
    }

    // ── Static factory methods ────────────────────────────────────────────────

    /** Build a fallback response with default reasoning */
    public static WorkloadRecommendationResponse fallback() {
        return fallback("Please select manually");
    }

    /** Build a fallback response with custom reasoning per SRS specification */
    public static WorkloadRecommendationResponse fallback(String reasoning) {
        return WorkloadRecommendationResponse.builder()
                .recommendedEmployeeId(null)
                .scoreBreakdown(List.of())
                .reasoning(reasoning)
                .fallbackUsed(true)
                .build();
    }
}
