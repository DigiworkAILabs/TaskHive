package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_PRIORITY = "MEDIUM";

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.model}")
    private String geminiModel;

    @Value("${ml.gemini.enabled:true}")
    private boolean geminiEnabled;

    public TaskPriorityResponse suggestPriority(TaskPriorityRequest request) {
        if (!geminiEnabled) {
            return TaskPriorityResponse.fallback();
        }

        try {
            String promptText = buildPrompt(request);
            
            // Construct Google Gemini JSON Request Body
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", promptText)
                    ))
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = String.format("%s%s:generateContent", geminiApiUrl, geminiModel);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("[GeminiService] Failed to get priority from Gemini: {}", e.getMessage());
            return TaskPriorityResponse.fallback();
        }
    }

    private String buildPrompt(TaskPriorityRequest request) {
        String safeDesc = (request.getTaskDescription() != null && !request.getTaskDescription().isBlank())
            ? request.getTaskDescription() : "No description provided";

        return String.format("""
            You are a task management AI for an enterprise system.
            Analyze this task and suggest the appropriate priority level.
            Return ONLY valid JSON. No explanation. No markdown formatting.
            === TASK ===
            Title: %s
            Description: %s
            
            Return ONLY in this format:
            {"predictedPriority":"MEDIUM","confidence":0.85,"reasoning":"one sentence"}
            
            Rules:
            - predictedPriority must be EXACTLY: LOW | MEDIUM | HIGH | CRITICAL
            - Login, payment, security, crash, data loss -> HIGH or CRITICAL
            - UI change, minor improvement, documentation -> LOW or MEDIUM
            - confidence: 0.0 to 1.0
            - reasoning: one sentence only""", request.getTaskTitle(), safeDesc);
    }

    private TaskPriorityResponse parseGeminiResponse(String jsonBody) {
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Remove markdown json formatting if Gemini includes it
            rawText = rawText.replace("```json", "").replace("```", "").trim();
            
            JsonNode resultNode = objectMapper.readTree(rawText);
            String priority = resultNode.path("predictedPriority").asText(DEFAULT_PRIORITY).toUpperCase();
            double confidence = resultNode.path("confidence").asDouble(0.5);
            String reasoning = resultNode.path("reasoning").asText("Gemini AI suggested priority");

            if (!List.of("LOW", DEFAULT_PRIORITY, "HIGH", "CRITICAL").contains(priority)) {
                priority = DEFAULT_PRIORITY;
            }

            return TaskPriorityResponse.of(priority, confidence, reasoning);

        } catch (Exception e) {
            log.error("[GeminiService] Failed to parse response: {}", e.getMessage());
            return TaskPriorityResponse.fallback();
        }
    }
}
