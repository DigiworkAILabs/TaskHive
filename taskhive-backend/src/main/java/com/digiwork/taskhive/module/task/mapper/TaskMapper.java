package com.digiwork.taskhive.module.task.mapper;

import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import com.digiwork.taskhive.common.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId().toString())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignedTo(task.getAssignedTo().toString())
                .assigneeName(resolveEmployeeName(task.getAssignedTo()))
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .estimatedHours(task.getEstimatedHours())
                .tags(task.getTags() != null ? Arrays.asList(task.getTags()) : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy() != null ? task.getCreatedBy().toString() : null)
                .updatedBy(task.getUpdatedBy() != null ? task.getUpdatedBy().toString() : null)
                .build();
    }

    public TaskListResponse toTaskListResponse(Task task) {
        return TaskListResponse.builder()
                .id(task.getId().toString())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assigneeName(resolveEmployeeName(task.getAssignedTo()))
                .dueDate(task.getDueDate())
                .tags(task.getTags() != null ? Arrays.asList(task.getTags()) : null)
                .createdAt(task.getCreatedAt())
                .build();
    }

    public TaskCommentResponse toTaskCommentResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId().toString())
                .taskId(comment.getTaskId().toString())
                .authorId(comment.getAuthorId().toString())
                .authorName(resolveUserName(comment.getAuthorId()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public TaskAttachmentResponse toTaskAttachmentResponse(TaskAttachment attachment) {
        return TaskAttachmentResponse.builder()
                .id(attachment.getId().toString())
                .taskId(attachment.getTaskId().toString())
                .uploadedBy(attachment.getUploadedBy().toString())
                .uploaderName(resolveUserName(attachment.getUploadedBy()))
                .fileName(attachment.getFileName())
                .fileUrl(storageService.getUrl(attachment.getFileUrl()))
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    public TaskStatusHistoryResponse toTaskStatusHistoryResponse(TaskStatusHistory history) {
        return TaskStatusHistoryResponse.builder()
                .id(history.getId().toString())
                .taskId(history.getTaskId().toString())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedBy(history.getChangedBy().toString())
                .changedByName(resolveUserName(history.getChangedBy()))
                .comment(history.getComment())
                .ipAddress(history.getIpAddress())
                .changedAt(history.getChangedAt())
                .build();
    }

    private String resolveEmployeeName(UUID employeeId) {
        if (employeeId == null)
            return null;
        return employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse(null);
    }

    private String resolveUserName(UUID userId) {
        if (userId == null)
            return null;
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse(null);
    }
}
