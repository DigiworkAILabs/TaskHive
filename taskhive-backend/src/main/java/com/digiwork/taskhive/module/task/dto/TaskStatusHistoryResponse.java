package com.digiwork.taskhive.module.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusHistoryResponse {

    private String id;
    private String taskId;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private String changedByName;
    private String comment;
    private String ipAddress;
    private LocalDateTime changedAt;
}
