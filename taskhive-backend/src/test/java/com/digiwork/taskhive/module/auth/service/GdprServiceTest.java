package com.digiwork.taskhive.module.auth.service;

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
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GdprServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCommentRepository taskCommentRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GdprService gdprService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("should export user data successfully")
    void shouldExportUserDataSuccessfully() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(Employee.builder().id(UUID.randomUUID()).build()));
        when(taskRepository.findByCreatedByAndIsDeletedFalse(userId)).thenReturn(new ArrayList<>());
        when(taskRepository.findByAssignedToAndIsDeletedFalse(userId)).thenReturn(new ArrayList<>());
        when(taskCommentRepository.findByAuthorId(userId)).thenReturn(new ArrayList<>());
        when(notificationRepository.countByUserId(userId)).thenReturn(5L);
        when(auditLogRepository.countByActorId(userId)).thenReturn(10L);

        GdprExportResponse response = gdprService.exportUserData(userId);

        assertThat(response).isNotNull();
        assertThat(response.user().email()).isEqualTo(user.getEmail());
        assertThat(response.notificationCount()).isEqualTo(5L);
        assertThat(response.auditLogCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("should throw Exception when export user not found")
    void shouldThrowException_whenExportUserNotFound() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gdprService.exportUserData(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("should anonymize user data successfully")
    void shouldAnonymizeUserDataSuccessfully() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-random-pass");
        when(employeeRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(Employee.builder().userId(userId).firstName("John").build()));

        gdprService.anonymizeUserData(userId);

        assertThat(user.getFirstName()).isEqualTo("[DELETED]");
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getIsDeleted()).isTrue();
        
        verify(userRepository).save(user);
        verify(employeeRepository).save(any(Employee.class));
        verify(eventPublisher).publishEvent(any(UserDataDeletedEvent.class));
    }

    @Test
    @DisplayName("should throw Exception when anonymize user not found")
    void shouldThrowException_whenAnonymizeUserNotFound() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gdprService.anonymizeUserData(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
