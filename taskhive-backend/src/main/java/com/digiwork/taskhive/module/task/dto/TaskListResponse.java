package com.digiwork.taskhive.module.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {

    private String id;
    private String title;
    private String status;
    private String priority;
    private String assigneeName;
    private LocalDateTime dueDate;
    private List<String> tags;
    private LocalDateTime createdAt;

    // Phase 1 v2.5
    private Boolean isLate;
}
