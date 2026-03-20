package com.digiwork.taskhive.module.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * P1.5 — Anomaly alert entry returned by getActiveAlerts().
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyAlertResponse {

    private String ruleId;
    private String description;
    private String severity;        // "HIGH", "MEDIUM", "LOW"
    private LocalDateTime detectedAt;
}
