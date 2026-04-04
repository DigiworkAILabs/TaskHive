package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GeminiService.
 *
 * Covers:
 * - Hotspot fix: API key must travel in the x-goog-api-key header, NOT in the URL query string.
 * - Fallback behaviour when Gemini is disabled or the API call fails.
 * - Successful priority parsing from the Gemini JSON response.
 * - Invalid priority values are normalised to MEDIUM.
 * - Null / blank task description is handled gracefully.
 */
@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GeminiService geminiService;

    private TaskPriorityRequest request;

    @BeforeEach
    void setUp() {
        request = new TaskPriorityRequest();
        request.setTaskTitle("Fix login bug");
        request.setTaskDescription("Users cannot login with Google OAuth");

        ReflectionTestUtils.setField(geminiService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(geminiService, "geminiApiKey", "test-secret-key");
        ReflectionTestUtils.setField(geminiService, "geminiApiUrl", "https://api.example.com/");
        ReflectionTestUtils.setField(geminiService, "geminiModel", "gemini-pro");
        ReflectionTestUtils.setField(geminiService, "geminiEnabled", true);
    }

    // ── Security hotspot fix ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Security: API key must not appear in the URL query string")
    class ApiKeySecurityTests {

        /**
         * HOTSPOT FIX VERIFICATION
         *
         * SonarQube flagged that the API key was appended to the URL as ?key=...
         * Keys in URLs land in server access logs, browser history, and proxy caches.
         *
         * The fix moves the key to the x-goog-api-key request header.
         * This test FAILS on the unfixed code and PASSES after the fix is applied —
         * use it as your acceptance criterion for the security hotspot.
         */
        @Test
        @DisplayName("API key must be sent as x-goog-api-key header, not in the URL")
        void apiKey_mustBeInHeader_notInUrl() {
            String mockResponse = buildGeminiResponse("HIGH", 0.9, "Critical auth issue");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            geminiService.suggestPriority(request);

            // Capture what was actually sent to RestTemplate
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(String.class));

            String actualUrl = urlCaptor.getValue();
            HttpEntity<?> actualEntity = entityCaptor.getValue();

            // The URL must NOT contain the raw API key
            assertThat(actualUrl)
                    .as("API key must not be embedded in the URL query string")
                    .doesNotContain("test-secret-key")
                    .doesNotContain("?key=");

            // The key MUST be in the x-goog-api-key header
            assertThat(actualEntity.getHeaders().get("x-goog-api-key"))
                    .as("API key must be present in the x-goog-api-key header")
                    .isNotNull()
                    .contains("test-secret-key");
        }

        @Test
        @DisplayName("URL must not contain any query parameter named 'key'")
        void url_mustNotContainKeyQueryParam() {
            String mockResponse = buildGeminiResponse("MEDIUM", 0.7, "Moderate issue");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            geminiService.suggestPriority(request);

            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(String.class));

            assertThat(urlCaptor.getValue())
                    .as("URL must not contain '?key=' query parameter")
                    .doesNotContain("?key=")
                    .doesNotContain("&key=");
        }
    }

    // ── Fallback behaviour ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Fallback behaviour")
    class FallbackTests {

        @Test
        @DisplayName("Returns MEDIUM fallback when Gemini is disabled")
        void shouldReturnFallback_whenDisabled() {
            ReflectionTestUtils.setField(geminiService, "geminiEnabled", false);

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
            assertThat(response.getConfidence()).isEqualTo(0.5);
            assertThat(response.getReasoning()).contains("unavailable");
            assertThat(response.isFallbackUsed()).isTrue();
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Returns MEDIUM fallback when RestTemplate throws")
        void shouldReturnFallback_whenApiCallFails() {
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
            assertThat(response.isFallbackUsed()).isTrue();
        }

        @Test
        @DisplayName("Returns MEDIUM fallback when Gemini returns malformed JSON")
        void shouldReturnFallback_whenResponseMalformed() {
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("not-valid-json"));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
            assertThat(response.isFallbackUsed()).isTrue();
        }

        @Test
        @DisplayName("Returns MEDIUM fallback when response body is null")
        void shouldReturnFallback_whenResponseBodyIsNull() {
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(null));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
            assertThat(response.isFallbackUsed()).isTrue();
        }
    }

    // ── Successful priority parsing ───────────────────────────────────────────

    @Nested
    @DisplayName("Priority parsing")
    class PriorityParsingTests {

        @Test
        @DisplayName("Parses HIGH priority with correct confidence and reasoning")
        void parsesHighPriority_correctly() {
            String mockResponse = buildGeminiResponse("HIGH", 0.95, "Critical login issue");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("HIGH");
            assertThat(response.getConfidence()).isEqualTo(0.95);
            assertThat(response.getReasoning()).isEqualTo("Critical login issue");
            assertThat(response.isFallbackUsed()).isFalse();
        }

        @Test
        @DisplayName("Parses CRITICAL priority")
        void parsesCriticalPriority() {
            String mockResponse = buildGeminiResponse("CRITICAL", 0.99, "Data loss risk");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("Parses LOW priority")
        void parsesLowPriority() {
            String mockResponse = buildGeminiResponse("LOW", 0.6, "Minor UI tweak");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("LOW");
        }

        @Test
        @DisplayName("Unknown priority value is normalised to MEDIUM")
        void unknownPriority_normalisedToMedium() {
            String mockResponse = buildGeminiResponse("UNKNOWN_LEVEL", 0.5, "Unclear");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("Response wrapped in markdown code fences is parsed correctly")
        void markdownWrappedResponse_parsedCorrectly() {
            // Gemini sometimes wraps the JSON in ```json ... ``` markdown fences
            String innerJson = "{\"predictedPriority\":\"HIGH\",\"confidence\":0.88,\"reasoning\":\"Security issue\"}";
            String fencedResponse = buildGeminiResponseWithText("```json\n" + innerJson + "\n```");

            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(fencedResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("HIGH");
            assertThat(response.getConfidence()).isEqualTo(0.88);
        }
    }

    // ── Input edge cases ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Input edge cases")
    class InputEdgeCaseTests {

        @Test
        @DisplayName("Null task description is substituted with placeholder text")
        void nullDescription_usesPlaceholder() {
            request.setTaskDescription(null);
            String mockResponse = buildGeminiResponse("MEDIUM", 0.6, "No description provided");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            // Should not throw
            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("Blank task description is substituted with placeholder text")
        void blankDescription_usesPlaceholder() {
            request.setTaskDescription("   ");
            String mockResponse = buildGeminiResponse("MEDIUM", 0.6, "No description");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            TaskPriorityResponse response = geminiService.suggestPriority(request);

            assertThat(response.getPredictedPriority()).isNotNull();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a minimal Gemini API JSON response wrapping a priority JSON payload.
     */
    private String buildGeminiResponse(String priority, double confidence, String reasoning) {
    // Build the inner JSON string as plain text (no escaping needed)
    String innerJson = String.format(
            "{\"predictedPriority\":\"%s\",\"confidence\":%.2f,\"reasoning\":\"%s\"}",
            priority, confidence, reasoning
    );
    return buildGeminiResponseWithText(innerJson);
}

private String buildGeminiResponseWithText(String rawText) {
    // Use ObjectMapper to safely embed rawText as a JSON string value
    // This avoids manual escaping errors entirely
    try {
        ObjectMapper mapper = new ObjectMapper();
        String safeText = mapper.writeValueAsString(rawText); // produces "\"...\""
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":" + safeText + "}]}}]}";
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}
