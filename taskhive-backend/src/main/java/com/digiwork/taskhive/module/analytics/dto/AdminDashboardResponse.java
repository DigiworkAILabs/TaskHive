package com.digiwork.taskhive.module.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalTasks;
    private long activeTasks;
    private long overdueTasks;
    private long completedTasks;
    private BigDecimal completionRate;
    private int totalEmployees;
    private BigDecimal avgCompletionHours;
    private LocalDate metricDate;
}
