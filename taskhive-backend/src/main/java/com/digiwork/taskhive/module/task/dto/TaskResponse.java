package com.digiwork.taskhive.module.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assignedTo;
    private String assigneeName;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private BigDecimal estimatedHours;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Phase 1 v2.5 fields
    private Boolean isLate;
    private Integer lateByMinutes;
    private LocalDateTime submittedAt;
    private Boolean proofRequired;
    private Boolean approvalRequired;
}
