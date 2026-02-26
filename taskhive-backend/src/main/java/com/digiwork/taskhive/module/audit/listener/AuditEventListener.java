package com.digiwork.taskhive.module.audit.listener;

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

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTH EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleUserAuthenticated(UserAuthenticatedEvent event) {
        log.debug("Audit: UserAuthenticatedEvent for user [{}]", event.getUserId());
        auditService.logAction(
                event.getUserId(),
                event.getEmail(),
                "USER_LOGIN",
                "USER",
                event.getUserId(),
                null, null, null, null);
    }

    @EventListener
    public void handleUserLoggedOut(UserLoggedOutEvent event) {
        log.debug("Audit: UserLoggedOutEvent for user [{}]", event.getUserId());
        auditService.logAction(
                event.getUserId(),
                null,
                "USER_LOGOUT",
                "USER",
                event.getUserId(),
                null, null, null, null);
    }

    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.debug("Audit: PasswordChangedEvent for user [{}]", event.getUserId());
        auditService.logAction(
                event.getUserId(),
                event.getEmail(),
                "PASSWORD_CHANGED",
                "USER",
                event.getUserId(),
                null, null, null, null);
    }

    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.debug("Audit: PasswordResetRequestedEvent for user [{}]", event.getUserId());
        auditService.logAction(
                event.getUserId(),
                event.getEmail(),
                "PASSWORD_RESET_REQUESTED",
                "USER",
                event.getUserId(),
                null, null, null, null);
    }

    @EventListener
    public void handleAccountActivated(AccountActivatedEvent event) {
        log.debug("Audit: AccountActivatedEvent for user [{}]", event.getUserId());
        auditService.logAction(
                event.getUserId(),
                event.getEmail(),
                "ACCOUNT_ACTIVATED",
                "USER",
                event.getUserId(),
                null, null, null, null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPLOYEE EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.debug("Audit: EmployeeCreatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(
                event.getUserId(),
                event.getEmail(),
                "EMPLOYEE_CREATED",
                "EMPLOYEE",
                event.getEmployeeId(),
                null, null, null, null);
    }

    @EventListener
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.debug("Audit: EmployeeUpdatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(
                event.getUpdatedBy(),
                null,
                "EMPLOYEE_UPDATED",
                "EMPLOYEE",
                event.getEmployeeId(),
                null, null, null, null);
    }

    @EventListener
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        log.debug("Audit: EmployeeActivatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(
                event.getActivatedBy(),
                null,
                "EMPLOYEE_ACTIVATED",
                "EMPLOYEE",
                event.getEmployeeId(),
                null, null, null, null);
    }

    @EventListener
    public void handleEmployeeDeactivated(EmployeeDeactivatedEvent event) {
        log.debug("Audit: EmployeeDeactivatedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(
                event.getDeactivatedBy(),
                null,
                "EMPLOYEE_DEACTIVATED",
                "EMPLOYEE",
                event.getEmployeeId(),
                null, null, null, null);
    }

    @EventListener
    public void handleEmployeeDeleted(EmployeeDeletedEvent event) {
        log.debug("Audit: EmployeeDeletedEvent for employee [{}]", event.getEmployeeId());
        auditService.logAction(
                event.getDeletedBy(),
                null,
                "EMPLOYEE_DELETED",
                "EMPLOYEE",
                event.getEmployeeId(),
                null, null, null, null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.debug("Audit: TaskCreatedEvent for task [{}]", event.getTaskId());
        auditService.logAction(
                event.getCreatedBy(),
                null,
                "TASK_CREATED",
                "TASK",
                event.getTaskId(),
                null, null, null, null);
    }

    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.debug("Audit: TaskUpdatedEvent for task [{}]", event.getTaskId());
        auditService.logAction(
                event.getUpdatedBy(),
                null,
                "TASK_UPDATED",
                "TASK",
                event.getTaskId(),
                null, null, null, null);
    }

    @EventListener
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.debug("Audit: TaskStatusChangedEvent for task [{}]", event.getTaskId());
        String beforeState = "{\"status\":\"" + event.getOldStatus() + "\"}";
        String afterState = "{\"status\":\"" + event.getNewStatus() + "\"}";
        auditService.logAction(
                event.getChangedBy(),
                null,
                "TASK_STATUS_CHANGED",
                "TASK",
                event.getTaskId(),
                beforeState, afterState, null, null);
    }

    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.debug("Audit: TaskAssignedEvent for task [{}] to employee [{}]",
                event.getTaskId(), event.getEmployeeId());
        String afterState = "{\"assignedTo\":\"" + event.getEmployeeId() + "\"}";
        auditService.logAction(
                event.getAssignedBy(),
                null,
                "TASK_ASSIGNED",
                "TASK",
                event.getTaskId(),
                null, afterState, null, null);
    }

    @EventListener
    public void handleTaskCommentAdded(TaskCommentAddedEvent event) {
        log.debug("Audit: TaskCommentAddedEvent for task [{}]", event.getTaskId());
        String afterState = "{\"commentId\":\"" + event.getCommentId() + "\"}";
        auditService.logAction(
                event.getAuthorId(),
                null,
                "TASK_COMMENT_ADDED",
                "TASK",
                event.getTaskId(),
                null, afterState, null, null);
    }
}
