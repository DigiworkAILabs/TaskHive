package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.dto.WorkloadRecommendationRequest;
import com.digiwork.taskhive.module.ml.dto.WorkloadRecommendationResponse;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MLService
 * ──────────
 * Orchestration layer between MLController and MLClientService.
 *
 * Responsibilities:
 * 1. Enrich the frontend request with employee performance data
 * fetched from DB (TaskRepository + EmployeeRepository).
 * 2. Build the exact payload shape expected by FastAPI.
 * 3. Delegate HTTP call to MLClientService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLService {

    private final MLClientService mlClientService;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    // ── Feature 1: Task Priority Suggestion ──────────────────────────────────

    /**
     * Predict task priority.
     *
     * @param request DTO from frontend (title, description, tags, estimatedHours,
     *                employeeId)
     * @return Predicted priority with confidence score
     */
    public TaskPriorityResponse predictPriority(TaskPriorityRequest request) {
        log.info("[MLService] Predicting priority for task: '{}'", request.getTaskTitle());

        // 1. Enrich with employee performance metrics
        double empCompletionRate = getEmpCompletionRate(request.getEmployeeId());
        double empAvgHours = getEmpAvgHours(request.getEmployeeId());
        String department = getDepartment(request.getEmployeeId());

        // 2. Build FastAPI payload (matches TaskPriorityRequest schema)
        Map<String, Object> payload = new HashMap<>();
        payload.put("task_title", request.getTaskTitle());
        payload.put("task_description", request.getTaskDescription() != null
                ? request.getTaskDescription()
                : "");
        payload.put("tags", request.getTags() != null
                ? request.getTags()
                : List.of());
        payload.put("estimated_hours", request.getEstimatedHours() != null
                ? request.getEstimatedHours()
                : 0.0);
        payload.put("emp_completion_rate", empCompletionRate);
        payload.put("emp_avg_hours", empAvgHours);
        payload.put("department", department);

        log.debug("[MLService] Enriched payload: {}", payload);

        // 3. Delegate to HTTP client (circuit breaker inside)
        return mlClientService.predictPriority(payload);
    }

    // ── Feature 3: Workload Balance Recommendation ───────────────────────────

    /**
     * Recommend the best employee to assign a task to.
     *
     * Enrichment logic:
     * - For each candidateEmployeeId, fetch active_tasks count from TaskRepository
     * - Fetch completion_rate, on_time_rate from task history
     * - Fetch department from EmployeeRepository to determine dept_match
     *
     * @param request DTO from frontend (taskTitle, taskPriority,
     *                taskEstimatedHours, candidateEmployeeIds)
     * @return Recommended employee with score breakdown
     */
    public WorkloadRecommendationResponse recommendWorkloadBalance(WorkloadRecommendationRequest request) {
        log.info("[MLService] Workload recommendation for '{}' with {} candidates",
                request.getTaskTitle(), request.getCandidateEmployeeIds().size());

        // 1. Build enriched candidate list for FastAPI
        List<Map<String, Object>> enrichedCandidates = new ArrayList<>();

        for (String candidateIdStr : request.getCandidateEmployeeIds()) {
            try {
                UUID candidateId = UUID.fromString(candidateIdStr);

                // Fetch all tasks assigned to this employee
                List<Task> allTasks = taskRepository.findByAssignedToAndIsDeletedFalse(candidateId);

                // Count active tasks (not DONE or CANCELLED)
                int activeTaskCount = (int) allTasks.stream()
                        .filter(t -> !("DONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus())))
                        .count();

                // Calculate completion rate
                long totalTasks = allTasks.size();
                long completedTasks = allTasks.stream()
                        .filter(t -> "DONE".equals(t.getStatus()))
                        .count();
                double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks : 0.75;

                // Calculate on-time rate
                long onTimeTasks = allTasks.stream()
                        .filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null
                                && t.getDueDate() != null && !t.getCompletedAt().isAfter(t.getDueDate()))
                        .count();
                double onTimeRate = completedTasks > 0 ? (double) onTimeTasks / completedTasks : 0.75;

                // Determine department match
                boolean deptMatch = false;
                Optional<Employee> empOpt = employeeRepository.findByIdAndIsDeletedFalse(candidateId);
                if (empOpt.isPresent()) {
                    deptMatch = true;
                }

                // Calculate avg hours per task
                double avgHoursPerTask = allTasks.stream()
                        .filter(t -> t.getEstimatedHours() != null)
                        .mapToDouble(t -> t.getEstimatedHours().doubleValue())
                        .average()
                        .orElse(4.0);

                Map<String, Object> candidate = new HashMap<>();
                candidate.put("employee_id", candidateIdStr);
                candidate.put("active_tasks", activeTaskCount);
                candidate.put("completion_rate", Math.round(completionRate * 100.0) / 100.0);
                candidate.put("on_time_rate", Math.round(onTimeRate * 100.0) / 100.0);
                candidate.put("avg_hours_per_task", Math.round(avgHoursPerTask * 10.0) / 10.0);
                candidate.put("dept_match", deptMatch);

                enrichedCandidates.add(candidate);

            } catch (IllegalArgumentException ex) {
                log.warn("[MLService] Invalid UUID: {}", candidateIdStr);
                // Add with safe defaults
                Map<String, Object> candidate = new HashMap<>();
                candidate.put("employee_id", candidateIdStr);
                candidate.put("active_tasks", 5);
                candidate.put("completion_rate", 0.75);
                candidate.put("on_time_rate", 0.75);
                candidate.put("avg_hours_per_task", 4.0);
                candidate.put("dept_match", false);
                enrichedCandidates.add(candidate);
            }
        }

        // 2. Build FastAPI payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("task_priority", request.getTaskPriority());
        payload.put("task_estimated_hours", request.getTaskEstimatedHours() != null
                ? request.getTaskEstimatedHours()
                : 4.0);
        payload.put("candidates", enrichedCandidates);

        log.debug("[MLService] Workload payload: {} candidates enriched", enrichedCandidates.size());

        // 3. Delegate to HTTP client (circuit breaker inside)
        return mlClientService.recommendWorkloadBalance(payload);
    }

    // ── Private enrichment helpers for Priority ──────────────────────────────

    private double getEmpCompletionRate(UUID employeeId) {
        if (employeeId == null)
            return 0.75;
        try {
            List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
            if (tasks.isEmpty())
                return 0.75;
            long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
            return (double) done / tasks.size();
        } catch (Exception e) {
            return 0.75;
        }
    }

    private double getEmpAvgHours(UUID employeeId) {
        if (employeeId == null)
            return 4.0;
        try {
            List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
            return tasks.stream()
                    .filter(t -> t.getEstimatedHours() != null)
                    .mapToDouble(t -> t.getEstimatedHours().doubleValue())
                    .average()
                    .orElse(4.0);
        } catch (Exception e) {
            return 4.0;
        }
    }

    private String getDepartment(UUID employeeId) {
        if (employeeId == null)
            return "Unknown";
        return employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .map(Employee::getDepartment)
                .orElse("Unknown");
    }
}
