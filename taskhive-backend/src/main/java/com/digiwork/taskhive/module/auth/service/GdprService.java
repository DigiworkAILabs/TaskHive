package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.common.exception.ResourceNotFoundException;
import com.digiwork.taskhive.module.audit.repository.AuditLogRepository;
import com.digiwork.taskhive.module.auth.dto.GdprExportResponse;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.event.UserDataDeletedEvent;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.notification.repository.NotificationRepository;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles GDPR compliance — personal data export and user anonymization
 * (NFR-SEC-13).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GdprService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private static final String ANONYMIZED_VALUE = "[DELETED]";

    // ─── GDPR Data Export ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GdprExportResponse exportUserData(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        Optional<Employee> employeeOpt = employeeRepository.findByUserIdAndIsDeletedFalse(userId);

        List<Task> tasksCreated = taskRepository.findByCreatedByAndIsDeletedFalse(userId);
        List<Task> tasksAssigned = taskRepository.findByAssignedToAndIsDeletedFalse(userId);
        List<TaskComment> comments = taskCommentRepository.findByAuthorId(userId);

        long notificationCount = notificationRepository.countByUserId(userId);
        long auditLogCount = auditLogRepository.countByActorId(userId);

        log.info("GDPR export requested for user: {}", userId);

        return new GdprExportResponse(
                mapUserData(user),
                employeeOpt.map(this::mapEmployeeData).orElse(null),
                tasksCreated.stream().map(this::mapTaskSummary).toList(),
                tasksAssigned.stream().map(this::mapTaskSummary).toList(),
                comments.stream().map(this::mapComment).toList(),
                notificationCount,
                auditLogCount,
                LocalDateTime.now());
    }

    // ─── GDPR Anonymization (Right to be Forgotten) ──────────────────────────

    @Transactional
    public void anonymizeUserData(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        String anonymizedEmail = "deleted_" + UUID.randomUUID() + "@anonymized.local";
        String invalidatedPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        // Anonymize User entity
        user.setEmail(anonymizedEmail);
        user.setFirstName(ANONYMIZED_VALUE);
        user.setLastName(ANONYMIZED_VALUE);
        user.setPasswordHash(invalidatedPasswordHash);
        user.setStatus(UserStatus.DELETED);
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // Anonymize Employee entity if exists
        employeeRepository.findByUserIdAndIsDeletedFalse(userId).ifPresent(employee -> {
            employee.setFirstName(ANONYMIZED_VALUE);
            employee.setLastName(ANONYMIZED_VALUE);
            employee.setEmail(anonymizedEmail);
            employee.setPhone(null);
            employee.setIsDeleted(true);
            employee.setDeletedAt(LocalDateTime.now());
            employeeRepository.save(employee);
        });

        // Publish event for other modules to react
        eventPublisher.publishEvent(new UserDataDeletedEvent(userId, LocalDateTime.now()));

        log.info("GDPR anonymization completed for user: {}", userId);
    }

    // ─── Private Mappers ─────────────────────────────────────────────────────

    private GdprExportResponse.UserData mapUserData(User user) {
        return new GdprExportResponse.UserData(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                "USER", // role resolved separately if needed
                user.getStatus().name(),
                user.getCreatedAt());
    }

    private GdprExportResponse.EmployeeData mapEmployeeData(Employee e) {
        return new GdprExportResponse.EmployeeData(
                e.getDepartment(),
                e.getDesignation(),
                e.getPhone(),
                e.getJoinDate());
    }

    private GdprExportResponse.TaskSummaryData mapTaskSummary(Task t) {
        return new GdprExportResponse.TaskSummaryData(
                t.getId(),
                t.getTitle(),
                t.getStatus(),
                t.getPriority(),
                t.getDueDate(),
                t.getCreatedAt());
    }

    private GdprExportResponse.CommentData mapComment(TaskComment c) {
        return new GdprExportResponse.CommentData(
                c.getTaskId(),
                c.getContent(),
                c.getCreatedAt());
    }
}
