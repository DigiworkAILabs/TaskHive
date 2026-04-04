package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.TaskAttachmentResponse;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import com.digiwork.taskhive.module.task.repository.TaskAttachmentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.digiwork.taskhive.module.task.enums.AttachmentPurpose;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;
    private final TaskMapper taskMapper;

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB per SRS NFR-SEC-10
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            // Images
            "image/jpeg", "image/png", "image/webp", "image/gif",
            // Documents
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            // Text & Archives
            "text/plain", "text/csv",
            "application/zip", "application/x-rar-compressed");
    private static final String ATTACHMENT_DIRECTORY = "tasks/attachments";

    @Transactional
    public TaskAttachmentResponse uploadAttachment(UUID taskId, MultipartFile file, String purposeStr) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // State check — cannot upload to completed/cancelled tasks
        if (com.digiwork.taskhive.module.task.enums.TaskStatus.DONE.name().equals(task.getStatus()) || 
            com.digiwork.taskhive.module.task.enums.TaskStatus.CANCELLED.name().equals(task.getStatus())) {
            throw new BusinessException("Cannot add attachments to a completed or cancelled task");
        }

        // EMPLOYEE can only attach to their assigned tasks
        if ("EMPLOYEE".equals(currentRole)) {
            var employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only add attachments to tasks assigned to you");
            }
        }

        // Validate file
        validateFile(file);

        // Resolve purpose — default to GENERAL if not supplied or unrecognised
        AttachmentPurpose purpose;
        try {
            purpose = (purposeStr != null && !purposeStr.isBlank())
                    ? AttachmentPurpose.valueOf(purposeStr.toUpperCase())
                    : AttachmentPurpose.GENERAL;
        } catch (IllegalArgumentException e) {
            purpose = AttachmentPurpose.GENERAL;
        }

        try {
            // Store file
            String filename = UUID.randomUUID().toString();
            String storedPath = storageService.store(file, ATTACHMENT_DIRECTORY, filename);

            // Create attachment record — purpose is persisted here (P1.1 fix)
            TaskAttachment attachment = TaskAttachment.builder()
                    .taskId(taskId)
                    .uploadedBy(currentUserId)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(storedPath)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .attachmentPurpose(purpose)   // ← was MISSING, causing proof check to always fail
                    .build();

            attachment = attachmentRepository.save(attachment);

            log.info("Attachment uploaded to task {} with purpose {}: {}", taskId, purpose, attachment.getId());

            return taskMapper.toTaskAttachmentResponse(attachment);

        } catch (IOException e) {
            log.error("Failed to upload attachment for task: {}", taskId, e);
            throw new BusinessException("Failed to upload attachment");
        }
    }

    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getAttachments(UUID taskId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        // Verify task exists
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // EMPLOYEE can only view attachments of their assigned tasks
        if ("EMPLOYEE".equals(currentRole)) {
            var employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only view attachments of tasks assigned to you");
            }
        }

        return attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(taskMapper::toTaskAttachmentResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds maximum limit of 20MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(
                    "File type not allowed. Supported: images, PDF, Office documents, text, CSV, ZIP, RAR");
        }
    }
}
