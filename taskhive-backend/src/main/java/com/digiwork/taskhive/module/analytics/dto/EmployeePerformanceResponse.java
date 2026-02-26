package com.digiwork.taskhive.module.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePerformanceResponse {

    private UUID employeeId;
    private String employeeName;
    private int tasksAssigned;
    private int tasksCompleted;
    private BigDecimal onTimeRate;
    private BigDecimal avgCompletionHours;
}
