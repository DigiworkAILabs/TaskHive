package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.task.dto.TaskAttachmentResponse;
import com.digiwork.taskhive.module.task.dto.TaskStatusHistoryResponse;
import com.digiwork.taskhive.module.task.service.TaskAttachmentService;
import com.digiwork.taskhive.module.task.service.TaskStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@taskSecurity.canAccessTask(#taskId)")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "purpose", required = false, defaultValue = "GENERAL") String purpose) {
        TaskAttachmentResponse attachment = attachmentService.uploadAttachment(taskId, file, purpose);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", attachment));
    }

    @GetMapping("/attachments")
    @PreAuthorize("@taskSecurity.canAccessTask(#taskId)")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable UUID taskId) {
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachments(taskId);
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved successfully", attachments));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("@taskSecurity.canAccessTask(#taskId)")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId) {
        Resource file = attachmentService.downloadAttachment(taskId, attachmentId);
        
        // Retrieve metadata for headers
        TaskAttachmentResponse metadata = attachmentService.getAttachments(taskId).stream()
                .filter(a -> a.getId().equals(attachmentId.toString()))
                .findFirst()
                .orElse(null);

        String filename = (metadata != null) ? metadata.getFileName() : "attachment.bin";
        String mimeType = (metadata != null && metadata.getMimeType() != null) ? metadata.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(file);
    }

    @GetMapping("/history")
    @PreAuthorize("@taskSecurity.canAccessTask(#taskId)")
    public ResponseEntity<ApiResponse<List<TaskStatusHistoryResponse>>> getStatusHistory(
            @PathVariable UUID taskId) {
        List<TaskStatusHistoryResponse> history = statusHistoryService.getHistory(taskId);
        return ResponseEntity.ok(ApiResponse.success("Status history retrieved successfully", history));
    }
}
