package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.dto.CompletionTimeResponse;
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

import java.util.Map;

/**
 * MLClientService
 * ────────────────
 * HTTP client layer that calls the FastAPI ML inference server.
 *
 * Circuit Breaker (Resilience4j):
 * - Name: "mlService"
 * - On failure → fallbackMethod returns TaskPriorityResponse.fallback()
 * - Admin NEVER sees an error — always receives a graceful MEDIUM default.
 *
 * ml.enabled=false short-circuits all calls without touching the ML server.
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

    /**
     * Forward an enriched priority request to FastAPI and return the prediction.
     * Wrapped in a Resilience4j circuit breaker — falls back on any exception.
     *
     * @param mlPayload Map already enriched with employee metrics by MLService
     * @return Predicted priority, confidence, and reasoning
     */
    @CircuitBreaker(name = "mlService", fallbackMethod = "predictPriorityFallback")
    public TaskPriorityResponse predictPriority(Map<String, Object> mlPayload) {
        if (!mlEnabled) {
            log.info("[MLClient] ml.enabled=false — returning fallback MEDIUM");
            return TaskPriorityResponse.fallback();
        }

        String url = mlBaseUrl + "/ml/predict/task-priority";
        log.debug("[MLClient] POST {} payload={}", url, mlPayload);

        try {
            ResponseEntity<MLPriorityApiResponse> response = mlRestTemplate.postForEntity(
                    url,
                    mlPayload,
                    MLPriorityApiResponse.class);

            MLPriorityApiResponse body = response.getBody();
            if (body == null) {
                throw new MLServiceUnavailableException("FastAPI returned empty response body");
            }

            log.info("[MLClient] Priority prediction: {} (confidence={})",
                    body.getPredictedPriority(), body.getConfidence());

            return TaskPriorityResponse.of(
                    body.getPredictedPriority(),
                    body.getConfidence(),
                    body.getReasoning());

        } catch (RestClientException ex) {
            log.warn("[MLClient] ML server unreachable: {}", ex.getMessage());
            throw new MLServiceUnavailableException("ML server unreachable", ex);
        }
    }

    /**
     * Fallback method — Resilience4j calls this automatically when the circuit is
     * open
     * or when predictPriority() throws any exception.
     */
    @SuppressWarnings("unused")
    private TaskPriorityResponse predictPriorityFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Circuit breaker triggered for priority prediction: {}", t.getMessage());
        return TaskPriorityResponse.fallback();
    }

    // ── Feature 2: Task Completion Time Estimation ───────────────────────────

    /**
     * Forward an enriched completion time request to FastAPI and return the
     * prediction.
     * Wrapped in a Resilience4j circuit breaker — falls back on any exception.
     *
     * @param mlPayload Map already enriched with employee metrics by MLService
     * @return Predicted estimated_hours, confidence_range, and reasoning
     */
    @CircuitBreaker(name = "mlService", fallbackMethod = "predictCompletionFallback")
    public CompletionTimeResponse predictCompletionTime(Map<String, Object> mlPayload) {
        if (!mlEnabled) {
            log.info("[MLClient] ml.enabled=false — returning fallback completion estimate");
            Double manual = (Double) mlPayload.get("manual_estimate");
            return CompletionTimeResponse.fallback(manual);
        }

        String url = mlBaseUrl + "/ml/predict/completion-time";
        log.debug("[MLClient] POST {} payload={}", url, mlPayload);

        try {
            ResponseEntity<MLCompletionApiResponse> response = mlRestTemplate.postForEntity(
                    url,
                    mlPayload,
                    MLCompletionApiResponse.class);

            MLCompletionApiResponse body = response.getBody();
            if (body == null) {
                throw new MLServiceUnavailableException("FastAPI returned empty response body");
            }

            log.info("[MLClient] Completion prediction: {} hrs", body.getEstimatedHours());

            return CompletionTimeResponse.of(
                    body.getEstimatedHours(),
                    body.getConfidenceRange() != null ? body.getConfidenceRange().getLow() : null,
                    body.getConfidenceRange() != null ? body.getConfidenceRange().getHigh() : null,
                    body.getReasoning());

        } catch (RestClientException ex) {
            log.warn("[MLClient] ML server unreachable: {}", ex.getMessage());
            throw new MLServiceUnavailableException("ML server unreachable", ex);
        }
    }

    /**
     * Fallback method — Resilience4j calls this automatically when the circuit is
     * open
     * or when predictCompletionTime() throws any exception.
     */
    @SuppressWarnings("unused")
    private CompletionTimeResponse predictCompletionFallback(Map<String, Object> payload, Throwable t) {
        log.warn("[MLClient] Circuit breaker triggered for completion prediction: {}", t.getMessage());
        Double manual = (Double) payload.get("manual_estimate");
        return CompletionTimeResponse.fallback(manual);
    }

    // ── Inner class: raw FastAPI response shape ───────────────────────────────

    /**
     * Maps the raw JSON from FastAPI:
     * { "predicted_priority": "HIGH", "confidence": 0.84, "reasoning": "..." }
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class MLPriorityApiResponse {
        private String predicted_priority;
        private Double confidence;
        private String reasoning;
        private Boolean fallback_used;

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

    /**
     * Maps the raw JSON from FastAPI for Completion Time
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class MLCompletionApiResponse {
        private Double estimated_hours;
        private MLConfidenceRange confidence_range;
        private String reasoning;
        private Boolean fallback_used;

        public Double getEstimatedHours() {
            return estimated_hours != null ? estimated_hours : 0.0;
        }

        public MLConfidenceRange getConfidenceRange() {
            return confidence_range;
        }

        public String getReasoning() {
            return reasoning != null ? reasoning : "";
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class MLConfidenceRange {
        private Double low;
        private Double high;
    }
}
