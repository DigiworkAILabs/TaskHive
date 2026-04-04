package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.ml.model.MLPredictionLog;
import com.digiwork.taskhive.module.ml.repository.MLPredictionLogRepository;
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final TaskCommentRepository taskCommentRepository;
    private final MLPredictionLogRepository predictionLogRepository;
    private final UserRepository userRepository;
    
    private static final double DEFAULT_COMPLETION_RATE = 0.75;
    private static final double DEFAULT_ON_TIME_RATE = 0.78;
    private static final double DEFAULT_AVG_HOURS = 4.0;
    private static final double DEFAULT_AVG_HOURS_HIGH = 6.2;
    private static final double DEFAULT_AVG_HOURS_MEDIUM = 3.8;

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
        payload.put("emp_avg_hours_high", DEFAULT_AVG_HOURS_HIGH);
        payload.put("emp_avg_hours_medium", DEFAULT_AVG_HOURS_MEDIUM);
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

    // ── Feature 4: Employee Productivity Scoring ──────────────────────────

    public ProductivityScoreResponse predictProductivityScore(UUID employeeId, int periodDays) {
        log.info("[MLService] Calculating productivity score for employee: {}, period: {} days", employeeId, periodDays);

        LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);

        long tasksAssigned = tasks.stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate))
                .count();

        long tasksCompleted = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null && t.getCompletedAt().isAfter(startDate))
                .count();

        long tasksOverdue = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()) && !"DONE".equals(t.getStatus()))
                .count();

        double onTimeRate = tasksCompleted > 0 ? (double) tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null
                        && t.getDueDate() != null && !t.getCompletedAt().isAfter(t.getDueDate())
                        && t.getCompletedAt().isAfter(startDate))
                .count() / tasksCompleted : 0.75;

        double avgCompletionHours = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()) && t.getEstimatedHours() != null && t.getCompletedAt() != null && t.getCompletedAt().isAfter(startDate))
                .mapToDouble(t -> t.getEstimatedHours().doubleValue())
                .average().orElse(4.0);

        long commentActivity = taskCommentRepository.countByAuthorIdAndCreatedAtAfter(employeeId, startDate);

        // Fetch previous score for trend
        Double prevScore = predictionLogRepository.findLatestByFeatureAndEmployee("PRODUCTIVITY", employeeId)
                .map(log -> (Double) log.getOutputData().get("score"))
                .orElse(null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("employee_id", employeeId.toString());
        payload.put("period_days", periodDays);
        payload.put("tasks_assigned", (int) tasksAssigned);
        payload.put("tasks_completed", (int) tasksCompleted);
        payload.put("tasks_overdue", (int) tasksOverdue);
        payload.put("on_time_rate", onTimeRate);
        payload.put("avg_completion_hours", avgCompletionHours);
        payload.put("comment_activity", (int) commentActivity);
        payload.put("prev_score", prevScore);

        ProductivityScoreResponse response = mlClientService.predictProductivityScore(payload);

        // Log the prediction for future trend/retraining
        savePredictionLog("PRODUCTIVITY", employeeId, payload, response);

        return response;
    }

    private void savePredictionLog(String type, UUID employeeId, Map<String, Object> input, Object output) {
        try {
            Map<String, Object> outputMap = new ObjectMapper().convertValue(output, new TypeReference<Map<String, Object>>() {});
            MLPredictionLog logEntry = MLPredictionLog.builder()
                    .featureType(type)
                    .employee(userRepository.findById(employeeId).orElse(null))
                    .inputData(input)
                    .outputData(outputMap)
                    .fallbackUsed(outputMap.get("fallbackUsed") != null && (boolean) outputMap.get("fallbackUsed"))
                    .build();
            predictionLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("[MLService] Failed to save ML log: {}", e.getMessage());
        }
    }

    // ── Metric Helpers ───────────────────────────────────────────────────────

    private double getEmpCompletionRate(UUID employeeId) {
        if (employeeId == null)
            return DEFAULT_COMPLETION_RATE;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        if (tasks.isEmpty())
            return DEFAULT_COMPLETION_RATE;
        long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        return (double) done / tasks.size();
    }

    private double getEmpAvgHours(UUID employeeId) {
        if (employeeId == null)
            return DEFAULT_AVG_HOURS;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        return tasks.stream().filter(t -> t.getEstimatedHours() != null)
                .mapToDouble(t -> t.getEstimatedHours().doubleValue()).average().orElse(DEFAULT_AVG_HOURS);
    }

    private String getDepartment(UUID employeeId) {
        if (employeeId == null)
            return "Unknown";
        return employeeRepository.findByIdAndIsDeletedFalse(employeeId).map(Employee::getDepartment).orElse("Unknown");
    }

    private double getEmpOnTimeRate(UUID employeeId) {
        if (employeeId == null)
            return DEFAULT_ON_TIME_RATE;
        List<Task> tasks = taskRepository.findByAssignedToAndIsDeletedFalse(employeeId);
        long completed = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        if (completed == 0)
            return DEFAULT_ON_TIME_RATE;
        long onTime = tasks.stream().filter(t -> "DONE".equals(t.getStatus()) && t.getCompletedAt() != null
                && t.getDueDate() != null && !t.getCompletedAt().isAfter(t.getDueDate())).count();
        return (double) onTime / completed;
    }

    private int getEmpActiveTasks(UUID employeeId) {
        if (employeeId == null)
            return 0;
        return (int) taskRepository.findByAssignedToAndIsDeletedFalse(employeeId).stream()
                .filter(t -> !("DONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus()))).count();
    }
}
