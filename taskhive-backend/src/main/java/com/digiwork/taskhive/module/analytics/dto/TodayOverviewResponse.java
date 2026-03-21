package com.digiwork.taskhive.module.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * P1.5 — Today's overview panel response (FR-P1-12).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayOverviewResponse {

    private int totalAssignedToday;
    private int completedToday;
    private int inProgress;
    private int pendingApproval;
    private int overdueToday;
    private int lateSubmissionsToday;
    private int activeAnomalies;
}
