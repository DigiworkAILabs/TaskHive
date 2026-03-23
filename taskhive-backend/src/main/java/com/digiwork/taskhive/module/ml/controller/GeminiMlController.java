package com.digiwork.taskhive.module.ml.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityRequest;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.service.GeminiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ml/gemini")
@RequiredArgsConstructor
public class GeminiMlController {

    private final GeminiService geminiService;

    @PostMapping("/predict/task-priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskPriorityResponse>> predictGeminiTaskPriority(
            @RequestBody @Valid TaskPriorityRequest request) {
        log.info("[GeminiMlController] Gemini Priority prediction for: '{}'", request.getTaskTitle());
        return ResponseEntity.ok(ApiResponse.success("Gemini priority prediction successful",
                geminiService.suggestPriority(request)));
    }
}
