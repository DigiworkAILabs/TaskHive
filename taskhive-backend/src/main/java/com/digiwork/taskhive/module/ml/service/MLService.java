package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.*;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLService {

    private final MLClientService mlClientService;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    // ── Feature 1: Task Priority Suggestion ──────────────────────────────────

    public TaskPriorityResponse predictPriority(TaskPriorityRequest request) {
        log.info("[MLService] Predicting priority for task: '{}'", request.getTaskTitle());
        Map<String, Object> payload = new HashMap<>();
        payload.put("task_title", request.getTaskTitle());
        payload.put("task_description", request.getTaskDescription() != null ? request.getTaskDescription() : "");
        payload.put("tags", request.getTags() != null ? request.getTags() : List.of());
        payload.put("estimated_hours", request.getEstimatedHours() != null ? request.getEstimatedHours() : 0.0);
        payload.put("emp_completion_rate", getEmpCompletionRate(request.getEmployeeId()));
        payload.put("emp_avg_hours", getEmpAvgHours(request.getEmployeeId()));
        payload.put("department", getDepartment(request.getEmployeeId()));
        return mlClientService.predictPriority(payload);
    }

    // ── Feature 2: Task Completion Time Estimation ───────────────────────────

    public CompletionTimeResponse predictCompletionTime(CompletionTimeRequest request) {
        log.info("[MLService] Predicting completion time for task: '{}'", request.getTaskTitle());
        Map<String, Object> payload = new HashMap<>();
        payload.put("task_title", request.getTaskTitle());
        payload.put("task_description", request.getTaskDescription() != null ? request.getTaskDescription() : "");
        payload.put("priority", request.getPriority());
        payload.put("emp_on_time_rate", getEmpOnTimeRate(request.getEmployeeId()));
        payload.put("emp_avg_hours_high", getEmpAvgHoursHigh(request.getEmployeeId()));
        payload.put("emp_avg_hours_medium", getEmpAvgHoursMedium(request.getEmployeeId()));
        payload.put("emp_active_tasks", getEmpActiveTasks(request.getEmployeeId()));
        payload.put("manual_estimate", request.getEstimatedHours());
        return mlClientService.predictCompletionTime(payload);
    }

    // ── Feature 3: Workload Balance Recommendation ───────────────────────────

    public WorkloadRecommendationResponse recommendWorkloadBalance(WorkloadRecommendationRequest request) {
        log.info("[MLService] Workload recommendation for '{}'", request.getTaskTitle());
        List<Map<String, Object>> enrichedCandidates = new ArrayList<>();
        for (String candidateIdStr : request.getCandidateEmployeeIds()) {
            try {
                UUID candidateId = UUID.fromString(candidateIdStr);
                List<Task> allTasks = taskRepository.findByAssignedToAndIsDeletedFalse(candidateId);
                int activeTaskCount = (int) allTasks.stream()
                        .filter(t -> !("DONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus())))
                        .count();
                long totalTasks = allTasks.size();
                long completedTasks = allTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
                double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks : 0.75;

                long onTimeTasks = allTasks.stream()
                        .filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null
                                && t.getDueDate() != null && !t.getCompletedAt().isAfter(t.getDueDate()))
                        .count();
                double onTimeRate = completedTasks > 0 ? (double) onTimeTasks / completedTasks : 0.75;

                boolean deptMatch = employeeRepository.findByIdAndIsDeletedFalse(candidateId).isPresent();
                double avgHoursPerTask = allTasks.stream()
                        .filter(t -> t.getEstimatedHours() != null)
                        .mapToDouble(t -> t.getEstimatedHours().doubleValue())
                        .average().orElse(4.0);

                Map<String, Object> candidate = new HashMap<>();
                candidate.put("employee_id", candidateIdStr);
                candidate.put("active_tasks", activeTaskCount);
                candidate.put("completion_rate", Math.round(completionRate * 100.0) / 100.0);
                candidate.put("on_time_rate", Math.round(onTimeRate * 100.0) / 100.0);
                candidate.put("avg_hours_per_task", Math.round(avgHoursPerTask * 10.0) / 10.0);
                candidate.put("dept_match", deptMatch);
                enrichedCandidates.add(candidate);
            } catch (Exception ex) {
                log.warn("[MLService] Error enriching candidate {}: {}", candidateIdStr, ex.getMessage());
            }
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("task_priority", request.getTaskPriority());
        payload.put("task_estimated_hours",
                request.getTaskEstimatedHours() != null ? request.getTaskEstimatedHours() : 4.0);
        payload.put("candidates", enrichedCandidates);
        return mlClientService.recommendWorkloadBalance(payload);
    }

    // ── Metric Helpers ───────────────────────────────────────────────────────

    private double getEmpCompletionRate(UUID employeeId) {
        if (employeeId == null)
            return 0.75;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        if (tasks.isEmpty())
            return 0.75;
        long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        return (double) done / tasks.size();
    }

    private double getEmpAvgHours(UUID employeeId) {
        if (employeeId == null)
            return 4.0;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        return tasks.stream().filter(t -> t.getEstimatedHours() != null)
                .mapToDouble(t -> t.getEstimatedHours().doubleValue()).average().orElse(4.0);
    }

    private String getDepartment(UUID employeeId) {
        if (employeeId == null)
            return "Unknown";
        return employeeRepository.findByIdAndIsDeletedFalse(employeeId).map(Employee::getDepartment).orElse("Unknown");
    }

    private double getEmpOnTimeRate(UUID employeeId) {
        if (employeeId == null)
            return 0.78;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        long completed = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        if (completed == 0)
            return 0.78;
        long onTime = tasks.stream().filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null
                && t.getDueDate() != null && !t.getCompletedAt().isAfter(t.getDueDate())).count();
        return (double) onTime / completed;
    }

    private double getEmpAvgHoursHigh(UUID employeeId) {
        return 6.2;
    }

    private double getEmpAvgHoursMedium(UUID employeeId) {
        return 3.8;
    }

    private int getEmpActiveTasks(UUID employeeId) {
        if (employeeId == null)
            return 0;
        return (int) taskRepository.findByAssignedToAndIsDeletedFalse(employeeId).stream()
                .filter(t -> !("DONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus()))).count();
    }
}
