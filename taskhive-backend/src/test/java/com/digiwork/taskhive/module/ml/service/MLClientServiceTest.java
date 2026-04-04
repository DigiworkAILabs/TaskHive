package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MLClientServiceTest {

    @Mock
    private RestTemplate mlRestTemplate;

    @InjectMocks
    private MLClientService mlClientService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mlClientService, "mlBaseUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(mlClientService, "mlEnabled", true);
    }

    @Test
    @DisplayName("should call predictPriority and map response")
    void shouldCallPredictPriority() {
        MLClientService.MLPriorityApiResponse apiResponse = new MLClientService.MLPriorityApiResponse();
        apiResponse.setPredictedPriority("HIGH");
        apiResponse.setConfidence(0.9);
        apiResponse.setReasoning("Critical task");

        when(mlRestTemplate.postForEntity(anyString(), any(), eq(MLClientService.MLPriorityApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        TaskPriorityResponse response = mlClientService.predictPriority(new HashMap<>());

        assertThat(response.getPredictedPriority()).isEqualTo("HIGH");
        assertThat(response.getConfidence()).isEqualTo(0.9);
        assertThat(response.getReasoning()).isEqualTo("Critical task");
    }

    @Test
    @DisplayName("should use fallback for predictPriority when disabled")
    void shouldFallbackPriorityWhenDisabled() {
        ReflectionTestUtils.setField(mlClientService, "mlEnabled", false);
        TaskPriorityResponse response = mlClientService.predictPriority(new HashMap<>());
        assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM"); // Default fallback
    }

    @Test
    @DisplayName("should call predictCompletionTime and map response")
    void shouldCallPredictCompletionTime() {
        MLClientService.MLCompletionApiResponse apiResponse = new MLClientService.MLCompletionApiResponse();
        apiResponse.setEstimatedHours(4.5);
        apiResponse.setFallbackUsed(false);
        
        MLClientService.MLConfidenceRange cr = new MLClientService.MLConfidenceRange();
        cr.setLow(4.0);
        cr.setHigh(5.0);
        apiResponse.setConfidenceRange(cr);

        when(mlRestTemplate.postForEntity(anyString(), any(), eq(MLClientService.MLCompletionApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        CompletionTimeResponse response = mlClientService.predictCompletionTime(new HashMap<>());

        assertThat(response.getEstimatedHours()).isEqualTo(4.5);
        assertThat(response.getConfidenceRange().getLow()).isEqualTo(4.0);
        assertThat(response.isFallbackUsed()).isFalse();
    }

    @Test
    @DisplayName("should call recommendWorkloadBalance and map response")
    void shouldCallRecommendWorkload() {
        MLClientService.MLWorkloadApiResponse apiResponse = new MLClientService.MLWorkloadApiResponse();
        apiResponse.setRecommendedEmployeeId("emp-1");
        apiResponse.setFallbackUsed(false);
        
        MLClientService.MLWorkloadScoreEntry score = new MLClientService.MLWorkloadScoreEntry();
        score.setEmployeeId("emp-1");
        score.setScore(90.0);
        apiResponse.setScoreBreakdown(Collections.singletonList(score));

        when(mlRestTemplate.postForEntity(anyString(), any(), eq(MLClientService.MLWorkloadApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        WorkloadRecommendationResponse response = mlClientService.recommendWorkloadBalance(new HashMap<>());

        assertThat(response.getRecommendedEmployeeId()).isEqualTo("emp-1");
        assertThat(response.getScoreBreakdown()).hasSize(1);
        assertThat(response.isFallbackUsed()).isFalse();
    }

    @Test
    @DisplayName("should call predictProductivityScore and map response")
    void shouldCallPredictProductivity() {
        MLClientService.MLProductivityApiResponse apiResponse = new MLClientService.MLProductivityApiResponse();
        apiResponse.setScore(85.0);
        apiResponse.setFallbackUsed(false);

        when(mlRestTemplate.postForEntity(anyString(), any(), eq(MLClientService.MLProductivityApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ProductivityScoreResponse response = mlClientService.predictProductivityScore(new HashMap<>());

        assertThat(response.getScore()).isEqualTo(85.0);
        assertThat(response.isFallbackUsed()).isFalse();
    }
}
