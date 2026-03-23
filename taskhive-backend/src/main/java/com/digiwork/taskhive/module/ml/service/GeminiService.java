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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            // { "contents": [{ "parts": [{"text": "prompt"}] }] }
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", promptText)
                    ))
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = String.format("%s%s:generateContent?key=%s", geminiApiUrl, geminiModel, geminiApiKey);

            RestTemplate rawRestTemplate = new RestTemplate();
            ResponseEntity<String> response = rawRestTemplate.postForEntity(url, entity, String.class);

            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("[GeminiService] Failed to get priority from Gemini: {}", e.getMessage());
            return TaskPriorityResponse.fallback();
        }
    }

    private String buildPrompt(TaskPriorityRequest request) {
        String safeDesc = (request.getTaskDescription() != null && !request.getTaskDescription().isBlank())
            ? request.getTaskDescription() : "No description provided";

        return String.format(
            "You are a task management AI for an enterprise system.\n" +
            "Analyze this task and suggest the appropriate priority level.\n" +
            "Return ONLY valid JSON. No explanation. No markdown formatting.\n" +
            "=== TASK ===\n" +
            "Title: %s\n" +
            "Description: %s\n\n" +
            "Return ONLY in this format:\n" +
            "{\"predictedPriority\":\"MEDIUM\",\"confidence\":0.85,\"reasoning\":\"one sentence\"}\n\n" +
            "Rules:\n" +
            "- predictedPriority must be EXACTLY: LOW | MEDIUM | HIGH | CRITICAL\n" +
            "- Login, payment, security, crash, data loss -> HIGH or CRITICAL\n" +
            "- UI change, minor improvement, documentation -> LOW or MEDIUM\n" +
            "- confidence: 0.0 to 1.0\n" +
            "- reasoning: one sentence only",
            request.getTaskTitle(), safeDesc
        );
    }

    private TaskPriorityResponse parseGeminiResponse(String jsonBody) {
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Remove markdown json formatting if Gemini includes it
            rawText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
            
            JsonNode resultNode = objectMapper.readTree(rawText);
            String priority = resultNode.path("predictedPriority").asText("MEDIUM").toUpperCase();
            double confidence = resultNode.path("confidence").asDouble(0.5);
            String reasoning = resultNode.path("reasoning").asText("Gemini AI suggested priority");

            if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(priority)) {
                priority = "MEDIUM";
            }

            return TaskPriorityResponse.of(priority, confidence, reasoning);

        } catch (Exception e) {
            log.error("[GeminiService] Failed to parse response: {}", e.getMessage());
            return TaskPriorityResponse.fallback();
        }
    }
}
