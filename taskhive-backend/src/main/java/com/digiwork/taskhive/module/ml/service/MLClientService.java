package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.*;
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

    // ── Priority ─────────────────────────────────────────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictPriorityFallback")
    public TaskPriorityResponse predictPriority(Map<String, Object> mlPayload) {
        if (!mlEnabled)
            return TaskPriorityResponse.fallback();
        String url = mlBaseUrl + "/ml/predict/task-priority";
        ResponseEntity<MLPriorityApiResponse> response = mlRestTemplate.postForEntity(url, mlPayload,
                MLPriorityApiResponse.class);
        MLPriorityApiResponse body = response.getBody();
        if (body == null)
            throw new MLServiceUnavailableException("Empty response");
        return TaskPriorityResponse.of(body.getPredictedPriority(), body.getConfidence(), body.getReasoning());
    }

    private TaskPriorityResponse predictPriorityFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Priority fallback: {}", t.getMessage());
        return TaskPriorityResponse.fallback();
    }

    // ── Completion Time ──────────────────────────────────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictCompletionFallback")
    public CompletionTimeResponse predictCompletionTime(Map<String, Object> mlPayload) {
        if (!mlEnabled)
            return CompletionTimeResponse.fallback((Double) mlPayload.get("manual_estimate"));
        String url = mlBaseUrl + "/ml/predict/completion-time";
        log.info("[MLClient] Calling completion estimation: {} with payload: {}", url, mlPayload);
        ResponseEntity<MLCompletionApiResponse> response = mlRestTemplate.postForEntity(url, mlPayload,
                MLCompletionApiResponse.class);
        MLCompletionApiResponse body = response.getBody();
        log.info("[MLClient] Received response from {}: {}", url, body);
        if (body == null)
            throw new MLServiceUnavailableException("Empty response from ML server at " + url);
        return CompletionTimeResponse.builder()
                .estimatedHours(body.getEstimatedHours())
                .confidenceRange(new CompletionTimeResponse.ConfidenceRange(
                        body.getConfidenceRange() != null ? body.getConfidenceRange().getLow() : null,
                        body.getConfidenceRange() != null ? body.getConfidenceRange().getHigh() : null))
                .reasoning(body.getReasoning())
                .fallbackUsed(body.getFallbackUsed())
                .build();
    }

    private CompletionTimeResponse predictCompletionFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Completion fallback: {}", t.getMessage());
        return CompletionTimeResponse.fallback((Double) payload.get("manual_estimate"));
    }

    // ── Workload ─────────────────────────────────────────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "recommendWorkloadFallback")
    public WorkloadRecommendationResponse recommendWorkloadBalance(Map<String, Object> mlPayload) {
        if (!mlEnabled)
            return WorkloadRecommendationResponse.fallback();
        String url = mlBaseUrl + "/ml/recommend/workload-balance";
        ResponseEntity<MLWorkloadApiResponse> response = mlRestTemplate.postForEntity(url, mlPayload,
                MLWorkloadApiResponse.class);
        MLWorkloadApiResponse body = response.getBody();
        if (body == null)
            throw new MLServiceUnavailableException("Empty response");
        List<WorkloadRecommendationResponse.EmployeeScoreBreakdown> scores = body.getScoreBreakdown().stream()
                .map(entry -> new WorkloadRecommendationResponse.EmployeeScoreBreakdown(entry.getEmployeeId(),
                        entry.getScore()))
                .collect(Collectors.toList());
        return new WorkloadRecommendationResponse(body.getRecommendedEmployeeId(), scores, body.getReasoning(),
                body.getFallbackUsed() != null ? body.getFallbackUsed() : false);
    }

    private WorkloadRecommendationResponse recommendWorkloadFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Workload fallback: {}", t.getMessage());
        return WorkloadRecommendationResponse.fallback();
    }

    // ── Productivity ──────────────────────────────────────────────────────────

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictProductivityFallback")
    public ProductivityScoreResponse predictProductivityScore(Map<String, Object> mlPayload) {
        if (!mlEnabled)
            return ProductivityScoreResponse.fallback();
        String url = mlBaseUrl + "/ml/score/employee";
        ResponseEntity<MLProductivityApiResponse> response = mlRestTemplate.postForEntity(url, mlPayload,
                MLProductivityApiResponse.class);
        MLProductivityApiResponse body = response.getBody();
        if (body == null)
            throw new MLServiceUnavailableException("Empty response");

        return ProductivityScoreResponse.builder()
                .score(body.getScore())
                .grade(body.getGrade())
                .breakdown(body.getBreakdown())
                .trend(body.getTrend())
                .reasoning(body.getReasoning())
                .fallbackUsed(body.getFallbackUsed() != null ? body.getFallbackUsed() : false)
                .build();
    }

    private ProductivityScoreResponse predictProductivityFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Productivity fallback: {}", t.getMessage());
        return ProductivityScoreResponse.fallback();
    }

    // ── Mapping DTOs ─────────────────────────────────────────────────────────

    @lombok.Data
    static class MLPriorityApiResponse {
        @JsonProperty("predicted_priority")
        private String predictedPriority;
        private Double confidence;
        private String reasoning;

        public String getPredictedPriority() {
            return predictedPriority;
        }
    }

    @lombok.Data
    static class MLCompletionApiResponse {
        @JsonProperty("estimated_hours")
        private Double estimatedHours;

        @JsonProperty("confidence_range")
        private MLConfidenceRange confidenceRange;

        private String reasoning;

        @JsonProperty("fallback_used")
        private Boolean fallbackUsed;

        public Double getEstimatedHours() {
            return estimatedHours != null ? estimatedHours : 0.0;
        }

        public MLConfidenceRange getConfidenceRange() {
            return confidenceRange;
        }

        public Boolean getFallbackUsed() {
            return fallbackUsed != null ? fallbackUsed : false;
        }
    }

    @lombok.Data
    static class MLConfidenceRange {
        @JsonProperty("low")
        private Double low;

        @JsonProperty("high")
        private Double high;
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

    @lombok.Data
    static class MLProductivityApiResponse {
        private Double score;
        private String grade;
        private Map<String, Double> breakdown;
        private String trend;
        private String reasoning;
        @JsonProperty("fallback_used")
        private Boolean fallbackUsed;
    }
}
