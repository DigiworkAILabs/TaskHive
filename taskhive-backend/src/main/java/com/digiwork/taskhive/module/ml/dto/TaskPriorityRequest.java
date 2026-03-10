package com.digiwork.taskhive.module.ml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * TaskPriorityRequest
 * ────────────────────
 * DTO received from the Next.js frontend via POST
 * /api/v1/ml/predict/task-priority.
 *
 * Spring Boot enriches this with employee performance data (completion rate,
 * avg hours)
 * fetched from the employee_performance_cache table before forwarding to
 * FastAPI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPriorityRequest {

    /** Task title typed by the admin */
    @NotBlank(message = "taskTitle is required")
    @Size(min = 2, max = 500, message = "taskTitle must be 2–500 characters")
    private String taskTitle;

    /** Optional task description */
    @Size(max = 5000, message = "taskDescription exceeds 5000 characters")
    private String taskDescription;

    /** UUID of the employee being assigned the task */
    private UUID employeeId;

    /** List of tags attached to the task */
    private List<String> tags;

    /** Admin-entered estimated hours (optional at this stage) */
    private Double estimatedHours;
}
