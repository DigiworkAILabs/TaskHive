package com.digiwork.taskhive.module.task.mapper;

import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    @org.springframework.beans.factory.annotation.Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

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
                // Phase 1 v2.5
                .isLate(task.getIsLate())
                .lateByMinutes(task.getLateByMinutes())
                .submittedAt(task.getSubmittedAt())
                .proofRequired(task.getProofRequired())
                .approvalRequired(task.getApprovalRequired())
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
                .isLate(task.getIsLate())   // Phase 1 v2.5
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
                .fileUrl(backendUrl + "/api/v1/tasks/" + attachment.getTaskId() + "/attachments/" + attachment.getId() + "/download")
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .attachmentPurpose(                                    // P1.1 fix
                        attachment.getAttachmentPurpose() != null
                                ? attachment.getAttachmentPurpose().name()
                                : "GENERAL")
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
