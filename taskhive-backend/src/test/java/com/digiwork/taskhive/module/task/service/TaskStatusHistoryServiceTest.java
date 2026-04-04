package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusHistoryServiceTest {

    @Mock
    private TaskStatusHistoryRepository historyRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskStatusHistoryService historyService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private UUID taskId;
    private UUID userId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("should allow ADMIN to view any task history")
    void shouldAllowAdminToViewAnyHistory() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("ADMIN");

        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(historyRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(new ArrayList<>());

        var result = historyService.getHistory(taskId);

        assertThat(result).isNotNull();
        verify(historyRepository).findByTaskIdOrderByChangedAtDesc(taskId);
    }

    @Test
    @DisplayName("should allow EMPLOYEE to view assigned task history")
    void shouldAllowEmployeeToViewOwnHistory() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("EMPLOYEE");

        Task task = Task.builder().id(taskId).assignedTo(employeeId).build();
        Employee employee = Employee.builder().id(employeeId).userId(userId).build();

        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(task));
        when(employeeRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(employee));
        when(historyRepository.findByTaskIdOrderByChangedAtDesc(taskId)).thenReturn(new ArrayList<>());

        var result = historyService.getHistory(taskId);

        assertThat(result).isNotNull();
        verify(historyRepository).findByTaskIdOrderByChangedAtDesc(taskId);
    }

    @Test
    @DisplayName("should block EMPLOYEE from viewing other's task history")
    void shouldBlockEmployeeFromOtherHistory() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("EMPLOYEE");

        Task task = Task.builder().id(taskId).assignedTo(UUID.randomUUID()).build(); // assigned to someone else
        Employee employee = Employee.builder().id(employeeId).userId(userId).build();

        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(task));
        when(employeeRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(employee));

        assertThrows(TaskAccessDeniedException.class, () -> historyService.getHistory(taskId));
    }

    @Test
    @DisplayName("should throw Exception when task not found")
    void shouldThrowException_whenTaskNotFound() {
        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> historyService.getHistory(taskId));
    }
}
