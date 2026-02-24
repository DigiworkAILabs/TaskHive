package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.task.dto.TaskCommentRequest;
import com.digiwork.taskhive.module.task.dto.TaskCommentResponse;
import com.digiwork.taskhive.module.task.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskCommentRequest request) {
        TaskCommentResponse comment = commentService.addComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskCommentResponse>>> getComments(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TaskCommentResponse> comments = commentService.getComments(taskId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }
}
