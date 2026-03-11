package com.digiwork.taskhive.module.ml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WorkloadRecommendationRequest
 * ──────────────────────────────
 * DTO received from the Next.js frontend via POST
 * /api/v1/ml/recommend/workload-balance.
 *
 * Spring Boot enriches the candidate employee list with performance stats
 * (active tasks, completion rate, on-time rate, dept match) from
 * TaskRepository + EmployeeRepository before forwarding to FastAPI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadRecommendationRequest {

    /** Task title (used for context) */
    @NotBlank(message = "taskTitle is required")
    @Size(min = 2, max = 500, message = "taskTitle must be 2–500 characters")
    private String taskTitle;

    /** Task priority: LOW / MEDIUM / HIGH / CRITICAL */
    @NotBlank(message = "taskPriority is required")
    private String taskPriority;

    /** Estimated hours to complete the task */
    private Double taskEstimatedHours;

    /** List of candidate employee IDs to evaluate */
    @NotEmpty(message = "candidateEmployeeIds must have at least one entry")
    private List<String> candidateEmployeeIds;
}
