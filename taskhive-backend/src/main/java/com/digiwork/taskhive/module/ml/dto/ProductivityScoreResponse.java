package com.digiwork.taskhive.module.ml.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ProductivityScoreResponse
 * ──────────────────────────
 * DTO returned to the Next.js frontend from GET
 * /api/v1/ml/score/employee/{id}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductivityScoreResponse {

    /** Productivity score from 0.0 to 100.0 */
    private Double score;

    /** Grade: A / B / C / D / F */
    private String grade;

    /** Component breakdown (completion, on-time, overdue, engagement) */
    private Map<String, Double> breakdown;

    /** Trend: improving / stable / declining */
    private String trend;

    /** Human-readable explanation of the score */
    private String reasoning;

    /** True when ML server was down and a default was returned */
    private boolean fallbackUsed;

    /** Build a fallback response per SRS specification */
    public static ProductivityScoreResponse fallback() {
        return ProductivityScoreResponse.builder()
                .score(0.0)
                .grade("F")
                .reasoning("ML model unavailable — fallback score returned.")
                .trend("stable")
                .fallbackUsed(true)
                .build();
    }
}
