package com.digiwork.taskhive.module.audit.listener;

import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.event.*;
import com.digiwork.taskhive.module.employee.event.*;
import com.digiwork.taskhive.module.task.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditEventListener auditEventListener;

    private UUID userId;
    private UUID employeeId;
    private UUID taskId;
    private String email;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        email = "test@example.com";
    }

    // ─── Auth Events ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleUserAuthenticated: should log USER_LOGIN")
    void handleUserAuthenticated() {
        UserAuthenticatedEvent event = new UserAuthenticatedEvent(this, userId, email);
        auditEventListener.handleUserAuthenticated(event);
        verify(auditService).logAction(argThat(log -> 
            "USER_LOGIN".equals(log.getAction()) && 
            userId.equals(log.getActorId()) &&
            email.equals(log.getActorEmail())
        ));
    }

    @Test
    @DisplayName("handleUserLoggedOut: should log USER_LOGOUT")
    void handleUserLoggedOut() {
        UserLoggedOutEvent event = new UserLoggedOutEvent(this, userId);
        auditEventListener.handleUserLoggedOut(event);
        verify(auditService).logAction(argThat(log -> 
            "USER_LOGOUT".equals(log.getAction()) && 
            userId.equals(log.getActorId())
        ));
    }

    @Test
    @DisplayName("handlePasswordChanged: should log PASSWORD_CHANGED")
    void handlePasswordChanged() {
        PasswordChangedEvent event = new PasswordChangedEvent(this, userId, email);
        auditEventListener.handlePasswordChanged(event);
        verify(auditService).logAction(argThat(log -> 
            "PASSWORD_CHANGED".equals(log.getAction()) && 
            userId.equals(log.getActorId())
        ));
    }

    @Test
    @DisplayName("handlePasswordResetRequested: should log PASSWORD_RESET_REQUESTED")
    void handlePasswordResetRequested() {
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(this, userId, email, "token");
        auditEventListener.handlePasswordResetRequested(event);
        verify(auditService).logAction(argThat(log -> 
            "PASSWORD_RESET_REQUESTED".equals(log.getAction()) && 
            userId.equals(log.getActorId())
        ));
    }

    @Test
    @DisplayName("handleAccountActivated: should log ACCOUNT_ACTIVATED")
    void handleAccountActivated() {
        AccountActivatedEvent event = new AccountActivatedEvent(this, userId, email);
        auditEventListener.handleAccountActivated(event);
        verify(auditService).logAction(argThat(log -> 
            "ACCOUNT_ACTIVATED".equals(log.getAction()) && 
            userId.equals(log.getActorId())
        ));
    }

    // ─── Employee Events ─────────────────────────────────────────────────────

    @Test
    @DisplayName("handleEmployeeCreated: should log EMPLOYEE_CREATED with employee as entity")
    void handleEmployeeCreated() {
        UUID createdBy = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(this, employeeId, userId, "John", email, "token", createdBy);
        auditEventListener.handleEmployeeCreated(event);
        verify(auditService).logAction(argThat(log -> 
            "EMPLOYEE_CREATED".equals(log.getAction()) && 
            createdBy.equals(log.getActorId()) &&
            employeeId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleEmployeeUpdated: should log EMPLOYEE_UPDATED")
    void handleEmployeeUpdated() {
        EmployeeUpdatedEvent event = new EmployeeUpdatedEvent(this, employeeId, userId);
        auditEventListener.handleEmployeeUpdated(event);
        verify(auditService).logAction(argThat(log -> 
            "EMPLOYEE_UPDATED".equals(log.getAction()) && 
            userId.equals(log.getActorId()) &&
            employeeId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleEmployeeActivated: should log EMPLOYEE_ACTIVATED")
    void handleEmployeeActivated() {
        EmployeeActivatedEvent event = new EmployeeActivatedEvent(this, employeeId, userId);
        auditEventListener.handleEmployeeActivated(event);
        verify(auditService).logAction(argThat(log -> 
            "EMPLOYEE_ACTIVATED".equals(log.getAction()) && 
            employeeId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleEmployeeDeactivated: should log EMPLOYEE_DEACTIVATED")
    void handleEmployeeDeactivated() {
        EmployeeDeactivatedEvent event = new EmployeeDeactivatedEvent(this, employeeId, userId);
        auditEventListener.handleEmployeeDeactivated(event);
        verify(auditService).logAction(argThat(log -> 
            "EMPLOYEE_DEACTIVATED".equals(log.getAction()) && 
            employeeId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleEmployeeDeleted: should log EMPLOYEE_DELETED")
    void handleEmployeeDeleted() {
        EmployeeDeletedEvent event = new EmployeeDeletedEvent(this, employeeId, userId);
        auditEventListener.handleEmployeeDeleted(event);
        verify(auditService).logAction(argThat(log -> 
            "EMPLOYEE_DELETED".equals(log.getAction()) && 
            employeeId.equals(log.getEntityId())
        ));
    }

    // ─── Task Events ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleTaskCreated: should log TASK_CREATED")
    void handleTaskCreated() {
        TaskCreatedEvent event = new TaskCreatedEvent(this, taskId, userId);
        auditEventListener.handleTaskCreated(event);
        verify(auditService).logAction(argThat(log -> 
            "TASK_CREATED".equals(log.getAction()) && 
            userId.equals(log.getActorId()) &&
            taskId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleTaskUpdated: should log TASK_UPDATED")
    void handleTaskUpdated() {
        TaskUpdatedEvent event = new TaskUpdatedEvent(this, taskId, userId);
        auditEventListener.handleTaskUpdated(event);
        verify(auditService).logAction(argThat(log -> 
            "TASK_UPDATED".equals(log.getAction()) && 
            taskId.equals(log.getEntityId())
        ));
    }

    @Test
    @DisplayName("handleTaskStatusChanged: should log TASK_STATUS_CHANGED with JSON state")
    void handleTaskStatusChanged() {
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(this, taskId, "TODO", "DONE", userId);
        auditEventListener.handleTaskStatusChanged(event);
        verify(auditService).logAction(argThat(log -> 
            "TASK_STATUS_CHANGED".equals(log.getAction()) && 
            log.getBeforeState().contains("TODO") &&
            log.getAfterState().contains("DONE")
        ));
    }

    @Test
    @DisplayName("handleTaskAssigned: should log TASK_ASSIGNED")
    void handleTaskAssigned() {
        TaskAssignedEvent event = new TaskAssignedEvent(this, taskId, employeeId, "Title", userId);
        auditEventListener.handleTaskAssigned(event);
        verify(auditService).logAction(argThat(log -> 
            "TASK_ASSIGNED".equals(log.getAction()) && 
            employeeId.toString().equals(log.getAfterState().replaceAll("[^a-zA-Z0-9-]", "").replace("assignedTo", ""))
            // Simplistic check for entityId and actorId as the JSON string check is brittle
            && taskId.equals(log.getEntityId())
            && userId.equals(log.getActorId())
        ));
    }

    @Test
    @DisplayName("handleTaskCommentAdded: should log TASK_COMMENT_ADDED")
    void handleTaskCommentAdded() {
        UUID commentId = UUID.randomUUID();
        TaskCommentAddedEvent event = new TaskCommentAddedEvent(this, taskId, commentId, userId);
        auditEventListener.handleTaskCommentAdded(event);
        verify(auditService).logAction(argThat(log -> 
            "TASK_COMMENT_ADDED".equals(log.getAction()) && 
            taskId.equals(log.getEntityId()) &&
            userId.equals(log.getActorId())
        ));
    }
}
