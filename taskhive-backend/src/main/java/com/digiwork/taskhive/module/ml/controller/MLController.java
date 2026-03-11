package com.digiwork.taskhive.module.ml.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.dto.WorkloadRecommendationRequest;
import com.digiwork.taskhive.module.ml.dto.WorkloadRecommendationResponse;
import com.digiwork.taskhive.module.ml.service.MLService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MLController
 * ─────────────
 * Exposes ML suggestion endpoints to the Next.js frontend.
 * All endpoints are ADMIN-only and non-blocking.
 *
 * Phase 7.1 endpoint:
 * POST /api/v1/ml/predict/task-priority
 *
 * Phase 7.3 endpoint:
 * POST /api/v1/ml/recommend/workload-balance
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ml")
@RequiredArgsConstructor
public class MLController {

        private final MLService mlService;

        // ── Feature 1: Task Priority Suggestion ──────────────────────────────────

        /**
         * POST /api/v1/ml/predict/task-priority
         *
         * Receives task details from the admin's Create Task form,
         * enriches with employee data, forwards to FastAPI, returns prediction.
         *
         * Always returns 200 OK — fallback MEDIUM is returned if ML is down.
         */
        @PostMapping("/predict/task-priority")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<TaskPriorityResponse>> predictTaskPriority(
                        @RequestBody @Valid TaskPriorityRequest request) {

                log.info("[MLController] Task priority prediction requested for: '{}'",
                                request.getTaskTitle());

                TaskPriorityResponse prediction = mlService.predictPriority(request);

                return ResponseEntity.ok(
                                ApiResponse.success("Priority prediction successful", prediction));
        }

        // ── Feature 3: Workload Balance Recommendation ───────────────────────────

        /**
         * POST /api/v1/ml/recommend/workload-balance
         *
         * Receives candidate employee IDs + task info from the admin's Create Task
         * form.
         * Spring Boot enriches each candidate with performance stats from DB,
         * forwards to FastAPI, returns the recommended employee with score breakdown.
         *
         * Always returns 200 OK — fallback { recommendedEmployeeId: null } if ML is
         * down.
         */
        @PostMapping("/recommend/workload-balance")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<WorkloadRecommendationResponse>> recommendWorkloadBalance(
                        @RequestBody @Valid WorkloadRecommendationRequest request) {

                log.info("[MLController] Workload recommendation requested with {} candidates",
                                request.getCandidateEmployeeIds().size());

                WorkloadRecommendationResponse recommendation = mlService.recommendWorkloadBalance(request);

                return ResponseEntity.ok(
                                ApiResponse.success("Workload recommendation successful", recommendation));
        }
}
