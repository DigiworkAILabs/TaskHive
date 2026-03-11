package com.digiwork.taskhive.module.ml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * CompletionTimeRequest
 * ─────────────────────
 * DTO received from the Next.js frontend via POST
 * /api/v1/ml/predict/completion-time.
 * 
 * Spring Boot enriches this with employee performance data before forwarding to
 * FastAPI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionTimeRequest {

    /** Task title typed by the admin */
    @NotBlank(message = "taskTitle is required")
    @Size(min = 2, max = 500, message = "taskTitle must be 2–500 characters")
    private String taskTitle;

    /** Optional task description */
    @Size(max = 5000, message = "taskDescription exceeds 5000 characters")
    private String taskDescription;

    /** Selected priority of the task */
    @NotBlank(message = "priority is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "Invalid priority")
    private String priority;

    /** UUID of the assigned employee whose performance to factor in */
    @NotNull(message = "employeeId is required")
    private UUID employeeId;

    /** Admin's optional manual estimation in hours */
    private Double estimatedHours;
}
