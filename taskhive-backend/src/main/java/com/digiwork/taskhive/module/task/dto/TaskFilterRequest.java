package com.digiwork.taskhive.module.task.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskFilterRequest {
    private String status;
    private String priority;
    private UUID assignedTo;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDir = "desc";
}
