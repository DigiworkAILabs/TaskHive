package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.dto.WorkloadRecommendationResponse;
import com.digiwork.taskhive.module.ml.exception.MLServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MLClientService
 * ────────────────
 * HTTP client layer that calls the FastAPI ML inference server.
 *
 * Circuit Breaker (Resilience4j):
 * - Name: "mlService"
 * - On failure → fallback methods return graceful default/fallback DTOs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLClientService {

    private final @Qualifier("mlRestTemplate") RestTemplate mlRestTemplate;

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlBaseUrl;

    @Value("${ml.enabled:true}")
    private boolean mlEnabled;

    // ── Feature 1: Task Priority Suggestion ──────────────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictPriorityFallback")
    public TaskPriorityResponse predictPriority(Map<String, Object> mlPayload) {
        if (!mlEnabled) {
            return TaskPriorityResponse.fallback();
        }

        String url = mlBaseUrl + "/ml/predict/task-priority";
        try {
            ResponseEntity<MLPriorityApiResponse> response = mlRestTemplate.postForEntity(
                    url,
                    mlPayload,
                    MLPriorityApiResponse.class);

            MLPriorityApiResponse body = response.getBody();
            if (body == null)
                throw new MLServiceUnavailableException("Empty response");

            return TaskPriorityResponse.of(
                    body.getPredictedPriority(),
                    body.getConfidence(),
                    body.getReasoning());

        } catch (RestClientException ex) {
            log.warn("[MLClient] Priority ML unreachable: {}", ex.getMessage());
            throw new MLServiceUnavailableException("ML unreachable", ex);
        }
    }

    private TaskPriorityResponse predictPriorityFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Priority fallback triggered: {}", t.getMessage());
        return TaskPriorityResponse.fallback();
    }

    // ── Feature 3: Workload Balance Recommendation ───────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "recommendWorkloadFallback")
    public WorkloadRecommendationResponse recommendWorkloadBalance(Map<String, Object> mlPayload) {
        if (!mlEnabled) {
            return WorkloadRecommendationResponse.fallback("ML is disabled in configuration.");
        }

        String url = mlBaseUrl + "/ml/recommend/workload-balance";
        try {
            ResponseEntity<MLWorkloadApiResponse> response = mlRestTemplate.postForEntity(
                    url,
                    mlPayload,
                    MLWorkloadApiResponse.class);

            MLWorkloadApiResponse body = response.getBody();
            if (body == null)
                throw new MLServiceUnavailableException("Empty response");

            List<WorkloadRecommendationResponse.EmployeeScoreBreakdown> scores = body.getScoreBreakdown().stream()
                    .map(entry -> new WorkloadRecommendationResponse.EmployeeScoreBreakdown(
                            entry.getEmployeeId(),
                            entry.getScore()))
                    .collect(Collectors.toList());

            return new WorkloadRecommendationResponse(
                    body.getRecommendedEmployeeId(),
                    scores,
                    body.getReasoning(),
                    body.getFallbackUsed() != null ? body.getFallbackUsed() : false);

        } catch (RestClientException ex) {
            log.warn("[MLClient] Workload ML unreachable: {}", ex.getMessage());
            throw new MLServiceUnavailableException("ML unreachable", ex);
        }
    }

    private WorkloadRecommendationResponse recommendWorkloadFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Workload fallback triggered: {}", t.getMessage());
        return WorkloadRecommendationResponse
                .fallback("AI Recommendation service is currently unavailable. Please select an assignee manually.");
    }

    // ── Internal DTOs for FastAPI Response Mapping ───────────────────────────

    @lombok.Data
    static class MLPriorityApiResponse {
        @JsonProperty("predicted_priority")
        private String predicted_priority;
        private Double confidence;
        private String reasoning;

        public String getPredictedPriority() {
            return predicted_priority;
        }

        public Double getConfidence() {
            return confidence != null ? confidence : 0.5;
        }

        public String getReasoning() {
            return reasoning != null ? reasoning : "";
        }
    }

    @lombok.Data
    static class MLWorkloadApiResponse {
        @JsonProperty("recommended_employee_id")
        private String recommendedEmployeeId;

        @JsonProperty("score_breakdown")
        private List<MLWorkloadScoreEntry> scoreBreakdown;

        private String reasoning;

        @JsonProperty("fallback_used")
        private Boolean fallbackUsed;
    }

    @lombok.Data
    static class MLWorkloadScoreEntry {
        @JsonProperty("employee_id")
        private String employeeId;
        private Double score;
    }
}
