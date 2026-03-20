package com.digiwork.taskhive.module.task.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private String priority;

    private LocalDateTime dueDate;

    private UUID assignedTo;

    private BigDecimal estimatedHours;

    private List<String> tags;

    /** P1.1 — null = no change, true/false = explicitly set proof requirement */
    private Boolean proofRequired;

    /** P1.2 — null = no change, true/false = explicitly set approval requirement */
    private Boolean approvalRequired;
}
