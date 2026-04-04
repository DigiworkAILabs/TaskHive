package com.digiwork.taskhive.module.audit.listener;

import com.digiwork.taskhive.module.audit.model.AuditLog;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.event.*;
import com.digiwork.taskhive.module.employee.event.*;
import com.digiwork.taskhive.module.task.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;
    private static final String ENTITY_EMPLOYEE = "EMPLOYEE";

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTH EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleUserAuthenticated(UserAuthenticatedEvent event) {
        log.debug("Audit: UserAuthenticatedEvent for user [{}]", event.getUserId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUserId())
                .actorEmail(event.getEmail())
                .action("USER_LOGIN")
                .entityType("USER")
                .entityId(event.getUserId())
                .build());
    }

    @EventListener
    public void handleUserLoggedOut(UserLoggedOutEvent event) {
        log.debug("Audit: UserLoggedOutEvent for user [{}]", event.getUserId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUserId())
                .action("USER_LOGOUT")
                .entityType("USER")
                .entityId(event.getUserId())
                .build());
    }

    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.debug("Audit: PasswordChangedEvent for user [{}]", event.getUserId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUserId())
                .actorEmail(event.getEmail())
                .action("PASSWORD_CHANGED")
                .entityType("USER")
                .entityId(event.getUserId())
                .build());
    }

    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.debug("Audit: PasswordResetRequestedEvent for user [{}]", event.getUserId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUserId())
                .actorEmail(event.getEmail())
                .action("PASSWORD_RESET_REQUESTED")
                .entityType("USER")
                .entityId(event.getUserId())
                .build());
    }

    @EventListener
    public void handleAccountActivated(AccountActivatedEvent event) {
        log.debug("Audit: AccountActivatedEvent for user [{}]", event.getUserId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUserId())
                .actorEmail(event.getEmail())
                .action("ACCOUNT_ACTIVATED")
                .entityType("USER")
                .entityId(event.getUserId())
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPLOYEE EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.debug("Audit: EmployeeCreatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getCreatedBy())   // admin who triggered creation, already committed
                .actorEmail(event.getEmail())
                .action("EMPLOYEE_CREATED")
                .entityType(ENTITY_EMPLOYEE)
                .entityId(event.getEmployeeId())
                .build());
    }

    @EventListener
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.debug("Audit: EmployeeUpdatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUpdatedBy())
                .action("EMPLOYEE_UPDATED")
                .entityType(ENTITY_EMPLOYEE)
                .entityId(event.getEmployeeId())
                .build());
    }

    @EventListener
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        log.debug("Audit: EmployeeActivatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getActivatedBy())
                .action("EMPLOYEE_ACTIVATED")
                .entityType(ENTITY_EMPLOYEE)
                .entityId(event.getEmployeeId())
                .build());
    }

    @EventListener
    public void handleEmployeeDeactivated(EmployeeDeactivatedEvent event) {
        log.debug("Audit: EmployeeDeactivatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getDeactivatedBy())
                .action("EMPLOYEE_DEACTIVATED")
                .entityType(ENTITY_EMPLOYEE)
                .entityId(event.getEmployeeId())
                .build());
    }

    @EventListener
    public void handleEmployeeDeleted(EmployeeDeletedEvent event) {
        log.debug("Audit: EmployeeDeletedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getDeletedBy())
                .action("EMPLOYEE_DELETED")
                .entityType(ENTITY_EMPLOYEE)
                .entityId(event.getEmployeeId())
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.debug("Audit: TaskCreatedEvent for task [{}]", event.getTaskId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getCreatedBy())
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(event.getTaskId())
                .build());
    }

    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.debug("Audit: TaskUpdatedEvent for task [{}]", event.getTaskId());
        auditService.logAction(AuditLog.builder()
                .actorId(event.getUpdatedBy())
                .action("TASK_UPDATED")
                .entityType("TASK")
                .entityId(event.getTaskId())
                .build());
    }

    @EventListener
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.debug("Audit: TaskStatusChangedEvent for task [{}]", event.getTaskId());
        String beforeState = "{\"status\":\"" + event.getOldStatus() + "\"}";
        String afterState = "{\"status\":\"" + event.getNewStatus() + "\"}";
        auditService.logAction(AuditLog.builder()
                .actorId(event.getChangedBy())
                .action("TASK_STATUS_CHANGED")
                .entityType("TASK")
                .entityId(event.getTaskId())
                .beforeState(beforeState)
                .afterState(afterState)
                .build());
    }

    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.debug("Audit: TaskAssignedEvent for task [{}] to employee [{}]",
                event.getTaskId(), event.getEmployeeId());
        String afterState = "{\"assignedTo\":\"" + event.getEmployeeId() + "\"}";
        auditService.logAction(AuditLog.builder()
                .actorId(event.getAssignedBy())
                .action("TASK_ASSIGNED")
                .entityType("TASK")
                .entityId(event.getTaskId())
                .afterState(afterState)
                .build());
    }

    @EventListener
    public void handleTaskCommentAdded(TaskCommentAddedEvent event) {
        log.debug("Audit: TaskCommentAddedEvent for task [{}]", event.getTaskId());
        String afterState = "{\"commentId\":\"" + event.getCommentId() + "\"}";
        auditService.logAction(AuditLog.builder()
                .actorId(event.getAuthorId())
                .action("TASK_COMMENT_ADDED")
                .entityType("TASK")
                .entityId(event.getTaskId())
                .afterState(afterState)
                .build());
    }
}
