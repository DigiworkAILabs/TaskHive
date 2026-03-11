package com.digiwork.taskhive.module.ml.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.ml.dto.*;
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
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ml")
@RequiredArgsConstructor
public class MLController {

        private final MLService mlService;

        /**
         * POST /api/v1/ml/predict/task-priority
         */
        @PostMapping("/predict/task-priority")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<TaskPriorityResponse>> predictTaskPriority(
                        @RequestBody @Valid TaskPriorityRequest request) {
                log.info("[MLController] Priority prediction for: '{}'", request.getTaskTitle());
                return ResponseEntity.ok(ApiResponse.success("Priority prediction successful",
                                mlService.predictPriority(request)));
        }

        /**
         * POST /api/v1/ml/predict/completion-time
         */
        @PostMapping("/predict/completion-time")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<CompletionTimeResponse>> predictCompletionTime(
                        @RequestBody @Valid CompletionTimeRequest request) {
                log.info("[MLController] Completion time prediction for: '{}'", request.getTaskTitle());
                return ResponseEntity.ok(ApiResponse.success("Completion time prediction successful",
                                mlService.predictCompletionTime(request)));
        }

        /**
         * POST /api/v1/ml/recommend/workload-balance
         */
        @PostMapping("/recommend/workload-balance")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<WorkloadRecommendationResponse>> recommendWorkloadBalance(
                        @RequestBody @Valid WorkloadRecommendationRequest request) {
                log.info("[MLController] Workload recommendation for {} candidates",
                                request.getCandidateEmployeeIds().size());
                return ResponseEntity.ok(ApiResponse.success("Workload recommendation successful",
                                mlService.recommendWorkloadBalance(request)));
        }
}
