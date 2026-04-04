package com.digiwork.taskhive.module.notification.listener;

import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.service.EmailQueueService;
import com.digiwork.taskhive.module.notification.service.NotificationService;
import com.digiwork.taskhive.module.task.event.TaskAssignedEvent;
import com.digiwork.taskhive.module.task.event.TaskCommentAddedEvent;
import com.digiwork.taskhive.module.task.event.TaskOverdueEvent;
import com.digiwork.taskhive.module.task.event.TaskStatusChangedEvent;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskEventListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailQueueService emailQueueService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskEventListener taskEventListener;

    private UUID taskId;
    private UUID employeeId;
    private UUID userId;
    private UUID adminUserId;
    private Employee sampleEmployee;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        sampleEmployee = Employee.builder()
                .id(employeeId)
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        sampleTask = Task.builder()
                .id(taskId)
                .title("Test Task")
                .assignedTo(employeeId)
                .priority("HIGH")
                .dueDate(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("handleTaskAssigned: should create notification with exact 'New Task' body")
    void handleTaskAssigned() {
        TaskAssignedEvent event = new TaskAssignedEvent(this, taskId, employeeId, "Test Task", UUID.randomUUID());
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(sampleEmployee));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        taskEventListener.handleTaskAssigned(event);

        verify(notificationService).createNotification(
            userId, 
            NotificationType.TASK_ASSIGNED, 
            "New Task Assigned", 
            "You have been assigned a new task: Test Task", 
            "TASK", 
            taskId
        );
    }

    @Test
    @DisplayName("handleTaskStatusChanged: should verify precise status transition text for employee and admin")
    void handleTaskStatusChanged() {
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(this, taskId, "TODO", "IN_PROGRESS", UUID.randomUUID());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(sampleEmployee));
        when(userRoleRepository.findUserIdsByRoleName("ADMIN")).thenReturn(List.of(adminUserId));

        taskEventListener.handleTaskStatusChanged(event);

        String expectedBody = "Task \"Test Task\" status changed from TODO to IN_PROGRESS";
        
        // Notify Employee
        verify(notificationService).createNotification(eq(userId), any(), any(), eq(expectedBody), any(), eq(taskId));
        
        // Notify Admin (includes requester name)
        verify(notificationService).createNotification(eq(adminUserId), any(), any(), contains("by John Doe"), any(), eq(taskId));
    }

    @Test
    @DisplayName("handleTaskStatusChanged: should notify admin of completion")
    void handleTaskStatusChanged_Completed() {
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(this, taskId, "IN_PROGRESS", "COMPLETED", UUID.randomUUID());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(sampleEmployee));
        when(userRoleRepository.findUserIdsByRoleName("ADMIN")).thenReturn(List.of(adminUserId));
        
        User adminUser = User.builder().id(adminUserId).email("admin@taskhive.com").firstName("Admin").lastName("User").build();
        when(userRepository.findByIdAndIsDeletedFalse(adminUserId)).thenReturn(Optional.of(adminUser));

        taskEventListener.handleTaskStatusChanged(event);

        verify(notificationService).createNotification(
            eq(adminUserId), 
            eq(NotificationType.TASK_COMPLETED), 
            eq("Task Completed"), 
            contains("has been completed by John Doe"), 
            eq("TASK"), 
            eq(taskId)
        );
    }

    @Test
    @DisplayName("handleTaskCommentAdded: should verify comment notification text")
    void handleTaskCommentAdded() {
        UUID authorId = UUID.randomUUID(); // Some other user
        TaskCommentAddedEvent event = new TaskCommentAddedEvent(this, taskId, UUID.randomUUID(), authorId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(sampleEmployee));
        when(userRoleRepository.findUserIdsByRoleName("ADMIN")).thenReturn(List.of(adminUserId));

        taskEventListener.handleTaskCommentAdded(event);

        String expectedBody = "A new comment was added to task: \"Test Task\"";
        verify(notificationService).createNotification(eq(userId), any(), eq("New Comment on Task"), eq(expectedBody), any(), eq(taskId));
    }

    @Test
    @DisplayName("handleTaskOverdue: should verify overdue alert text")
    void handleTaskOverdue() {
        TaskOverdueEvent event = new TaskOverdueEvent(this, taskId, employeeId, "Test Task", LocalDateTime.now(), 5L, "WARNING");
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(sampleEmployee));
        when(userRoleRepository.findUserIdsByRoleName("ADMIN")).thenReturn(List.of(adminUserId));
        
        User adminUser = User.builder().id(adminUserId).email("admin@taskhive.com").firstName("Admin").lastName("User").build();
        when(userRepository.findByIdAndIsDeletedFalse(adminUserId)).thenReturn(Optional.of(adminUser));

        taskEventListener.handleTaskOverdue(event);

        // Employee notification
        verify(notificationService).createNotification(eq(userId), any(), any(), contains("is 5 day(s) overdue"), any(), any());
        
        // Admin notification
        verify(notificationService).createNotification(eq(adminUserId), any(), any(), contains("assigned to John Doe is 5 day(s) overdue"), any(), any());
    }
}
