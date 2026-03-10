package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MLService
 * ──────────
 * Orchestration layer between MLController and MLClientService.
 *
 * Responsibilities:
 * 1. Enrich the frontend request with employee performance data
 * (emp_completion_rate, emp_avg_hours, department) fetched from DB.
 * 2. Build the exact payload shape expected by FastAPI.
 * 3. Delegate HTTP call to MLClientService.
 *
 * Note: Employee performance enrichment uses mock defaults in Phase 7.1.
 * In Phase 7.2+ this will query employee_performance_cache table via JPA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLService {

    private final MLClientService mlClientService;

    // ── Future dependency (Phase 7.2+):
    // private final EmployeePerformanceCacheRepository performanceCacheRepository;

    /**
     * Predict task priority.
     *
     * Enrichment logic:
     * - emp_completion_rate: from performance cache (fallback: 0.75)
     * - emp_avg_hours: from performance cache (fallback: 4.0)
     * - department: from employee record (fallback: "Unknown")
     *
     * @param request DTO from frontend (title, description, tags, estimatedHours,
     *                employeeId)
     * @return Predicted priority with confidence score
     */
    public TaskPriorityResponse predictPriority(TaskPriorityRequest request) {
        log.info("[MLService] Predicting priority for task: '{}'", request.getTaskTitle());

        // 1. Enrich with employee performance metrics
        // Phase 7.1: using safe defaults. Phase 7.2+ will query DB.
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

    // ── Private enrichment helpers ────────────────────────────────────────────
    // Phase 7.1: returns safe defaults.
    // Phase 7.2+: replace with DB queries to employee_performance_cache.

    private double getEmpCompletionRate(UUID employeeId) {
        // TODO Phase 7.2: query employee_performance_cache.on_time_rate by employeeId
        return 0.75;
    }

    private double getEmpAvgHours(UUID employeeId) {
        // TODO Phase 7.2: query employee_performance_cache.avg_completion_hours by
        // employeeId
        return 4.0;
    }

    private String getDepartment(UUID employeeId) {
        // TODO Phase 7.2: query employees.department by employeeId
        return "Unknown";
    }
}
