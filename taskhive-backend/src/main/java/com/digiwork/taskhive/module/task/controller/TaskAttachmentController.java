package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.task.dto.TaskAttachmentResponse;
import com.digiwork.taskhive.module.task.dto.TaskStatusHistoryResponse;
import com.digiwork.taskhive.module.task.service.TaskAttachmentService;
import com.digiwork.taskhive.module.task.service.TaskStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentService attachmentService;
    private final TaskStatusHistoryService statusHistoryService;

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {
        TaskAttachmentResponse attachment = attachmentService.uploadAttachment(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", attachment));
    }

    @GetMapping("/attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable UUID taskId) {
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachments(taskId);
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved successfully", attachments));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TaskStatusHistoryResponse>>> getStatusHistory(
            @PathVariable UUID taskId) {
        List<TaskStatusHistoryResponse> history = statusHistoryService.getHistory(taskId);
        return ResponseEntity.ok(ApiResponse.success("Status history retrieved successfully", history));
    }
}
