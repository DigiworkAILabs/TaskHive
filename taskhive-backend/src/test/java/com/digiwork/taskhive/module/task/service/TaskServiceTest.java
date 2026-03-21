package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

        @Mock
        private TaskRepository taskRepository;
        @Mock
        private TaskStatusHistoryRepository statusHistoryRepository;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private TaskMapper taskMapper;
        @Mock
        private ApplicationEventPublisher eventPublisher;

        @InjectMocks
        private TaskService taskService;

        private UUID currentUserId;
        private UUID taskId;
        private UUID employeeId;
        private Task testTask;

        @BeforeEach
        void setUp() {
                currentUserId = UUID.randomUUID();
                taskId = UUID.randomUUID();
                employeeId = UUID.randomUUID();

                testTask = Task.builder()
                                .id(taskId)
                                .title("Test Task")
                                .description("Test Description")
                                .status("TODO")
                                .priority("MEDIUM")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(7))
                                .isDeleted(false)
                                .createdBy(currentUserId)
                                .build();

                setSecurityContext(currentUserId, "ADMIN");
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        private void setSecurityContext(UUID userId, String role) {
                CustomUserDetails userDetails = new CustomUserDetails(
                                userId, "admin@example.com", "password", true,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // createTask Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("createTask")
        class CreateTaskTests {

                @Test
                @DisplayName("should create task successfully")
                void shouldCreateTaskSuccessfully() {
                        CreateTaskRequest request = new CreateTaskRequest();
                        request.setTitle("New Task");
                        request.setDescription("Description");
                        request.setPriority("HIGH");
                        request.setAssignedTo(employeeId);
                        request.setDueDate(LocalDateTime.now().plusDays(7));

                        Employee employee = Employee.builder().id(employeeId).status("ACTIVE").build();
                        TaskResponse expectedResponse = TaskResponse.builder()
                                        .id(taskId.toString()).title("New Task").build();

                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                                        .thenReturn(Optional.of(employee));
                        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
                        when(taskMapper.toTaskResponse(testTask)).thenReturn(expectedResponse);

                        TaskResponse result = taskService.createTask(request);

                        assertThat(result.getTitle()).isEqualTo("New Task");
                        verify(taskRepository).save(any(Task.class));
                        verify(statusHistoryRepository).save(any());
                        verify(eventPublisher, times(2)).publishEvent(any());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getAllTasks Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getAllTasks")
        class GetAllTasksTests {

                @Test
                @DisplayName("should return paginated tasks with filters")
                void shouldGetAllTasksWithFilters() {
                        Page<Task> page = new PageImpl<>(List.of(testTask));
                        TaskListResponse listResponse = TaskListResponse.builder()
                                        .id(taskId.toString()).title("Test Task").build();

                        when(taskRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                                        .thenReturn(page);
                        when(taskMapper.toTaskListResponse(testTask)).thenReturn(listResponse);

                        PageResponse<TaskListResponse> result = taskService.getAllTasks(
                                        null, null, null, null, null, 0, 10, "createdAt", "desc");

                        assertThat(result.getContent()).hasSize(1);
                        assertThat(result.getTotalElements()).isEqualTo(1);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getTaskById Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getTaskById")
        class GetTaskByIdTests {

                @Test
                @DisplayName("should return task by ID")
                void shouldGetTaskById() {
                        TaskResponse expectedResponse = TaskResponse.builder()
                                        .id(taskId.toString()).title("Test Task").build();

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId))
                                        .thenReturn(Optional.of(testTask));
                        when(taskMapper.toTaskResponse(testTask)).thenReturn(expectedResponse);

                        TaskResponse result = taskService.getTaskById(taskId);

                        assertThat(result.getId()).isEqualTo(taskId.toString());
                }

                @Test
                @DisplayName("should throw TaskNotFoundException when not found")
                void shouldThrowException_whenTaskNotFound() {
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                                        .isInstanceOf(TaskNotFoundException.class);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // updateTask Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("updateTask")
        class UpdateTaskTests {

                @Test
                @DisplayName("should update task successfully")
                void shouldUpdateTaskSuccessfully() {
                        UpdateTaskRequest request = new UpdateTaskRequest();
                        request.setTitle("Updated Task");
                        TaskResponse expectedResponse = TaskResponse.builder()
                                        .id(taskId.toString()).title("Updated Task").build();

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId))
                                        .thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
                        when(taskMapper.toTaskResponse(testTask)).thenReturn(expectedResponse);

                        TaskResponse result = taskService.updateTask(taskId, request);

                        assertThat(result.getTitle()).isEqualTo("Updated Task");
                        verify(eventPublisher).publishEvent(any());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // softDeleteTask Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("softDeleteTask")
        class SoftDeleteTaskTests {

                @Test
                @DisplayName("should soft delete task")
                void shouldSoftDeleteTask() {
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId))
                                        .thenReturn(Optional.of(testTask));

                        taskService.softDeleteTask(taskId);

                        assertThat(testTask.getIsDeleted()).isTrue();
                        verify(taskRepository).save(testTask);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getMyTasks Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getMyTasks")
        class GetMyTasksTests {

                @Test
                @DisplayName("should return tasks assigned to current user's employee record")
                void shouldGetMyTasks() {
                        Employee currentEmployee = Employee.builder().id(employeeId).userId(currentUserId).build();
                        Page<Task> page = new PageImpl<>(List.of(testTask));
                        TaskListResponse listResponse = TaskListResponse.builder()
                                        .id(taskId.toString()).title("Test Task").build();

                        when(employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId))
                                        .thenReturn(Optional.of(currentEmployee));
                        when(taskRepository.findMyTasksWithFilters(eq(employeeId), isNull(), isNull(), any(Pageable.class)))
                                        .thenReturn(page);
                        when(taskMapper.toTaskListResponse(testTask)).thenReturn(listResponse);

                        PageResponse<TaskListResponse> result = taskService.getMyTasks(0, 10, "createdAt", "desc", null, null);

                        assertThat(result.getContent()).hasSize(1);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getOverdueTasks Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getOverdueTasks")
        class GetOverdueTasksTests {

                @Test
                @DisplayName("should return overdue tasks")
                void shouldGetOverdueTasks() {
                        testTask.setDueDate(LocalDateTime.now().minusDays(1));
                        Page<Task> page = new PageImpl<>(List.of(testTask));
                        TaskListResponse listResponse = TaskListResponse.builder()
                                        .id(taskId.toString()).title("Test Task").build();

                        when(taskRepository.findOverdueTasksPaged(any(LocalDateTime.class), any(Pageable.class)))
                                        .thenReturn(page);
                        when(taskMapper.toTaskListResponse(testTask)).thenReturn(listResponse);

                        PageResponse<TaskListResponse> result = taskService.getOverdueTasks(0, 10);

                        assertThat(result.getContent()).hasSize(1);
                }
        }
}
