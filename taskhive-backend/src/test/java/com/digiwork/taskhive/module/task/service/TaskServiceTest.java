package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.enums.AttachmentPurpose;
import com.digiwork.taskhive.module.task.exception.ProofEnforcementException;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskAttachmentRepository;
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
        private TaskAttachmentRepository attachmentRepository;
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

                @Test
                @DisplayName("should throw exception when priority is invalid")
                void shouldThrowException_whenInvalidPriority() {
                        CreateTaskRequest request = new CreateTaskRequest();
                        request.setTitle("New Task");
                        request.setPriority("INVALID");
                        request.setDueDate(LocalDateTime.now().plusDays(1));

                        assertThatThrownBy(() -> taskService.createTask(request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Invalid priority");
                }

                @Test
                @DisplayName("should throw exception when due date is in past")
                void shouldThrowException_whenPastDueDate() {
                        CreateTaskRequest request = new CreateTaskRequest();
                        request.setTitle("New Task");
                        request.setPriority("HIGH");
                        request.setDueDate(LocalDateTime.now().minusDays(1));

                        assertThatThrownBy(() -> taskService.createTask(request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Due date must be in the future");
                }

                @Test
                @DisplayName("should throw exception when assignee is not active")
                void shouldThrowException_whenAssigneeNotActive() {
                        CreateTaskRequest request = new CreateTaskRequest();
                        request.setTitle("New Task");
                        request.setPriority("HIGH");
                        request.setAssignedTo(employeeId);
                        request.setDueDate(LocalDateTime.now().plusDays(1));

                        Employee inactiveEmployee = Employee.builder().id(employeeId).status("INACTIVE").build();
                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                                .thenReturn(Optional.of(inactiveEmployee));

                        assertThatThrownBy(() -> taskService.createTask(request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Task can only be assigned to an active employee");
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

                        TaskFilterRequest filter = TaskFilterRequest.builder()
                                        .page(0).size(10).sortBy("createdAt").sortDir("desc")
                                        .build();
                        PageResponse<TaskListResponse> result = taskService.getAllTasks(filter);

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

                @Test
                @DisplayName("EMPLOYEE should not view others' tasks")
                void shouldThrowException_whenEmployeeViewsOthersTask() {
                        setSecurityContext(currentUserId, "EMPLOYEE");
                        UUID otherEmployeeId = UUID.randomUUID();
                        testTask.setAssignedTo(otherEmployeeId);

                        Employee currentEmployee = Employee.builder().id(employeeId).userId(currentUserId).build();
                        when(employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId))
                                .thenReturn(Optional.of(currentEmployee));
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId))
                                .thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                                .isInstanceOf(TaskAccessDeniedException.class)
                                .hasMessageContaining("You can only view tasks assigned to you");
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

                @Test
                @DisplayName("should throw exception when updating with inactive assignee")
                void shouldThrowException_whenUpdatingToInactiveAssignee() {
                        UpdateTaskRequest request = new UpdateTaskRequest();
                        request.setAssignedTo(employeeId);

                        Employee inactiveEmployee = Employee.builder().id(employeeId).status("INACTIVE").build();
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                                .thenReturn(Optional.of(inactiveEmployee));

                        assertThatThrownBy(() -> taskService.updateTask(taskId, request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Task can only be assigned to an active employee");
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
        // updateTaskStatus Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("updateTaskStatus")
        class UpdateTaskStatusTests {

                @Test
                @DisplayName("should transition from TODO to IN_PROGRESS")
                void shouldTransitionSuccessfully() {
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_PROGRESS");
                        request.setComment("Starting work");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);
                        when(taskMapper.toTaskResponse(any())).thenReturn(TaskResponse.builder().status("IN_PROGRESS").build());

                        TaskResponse result = taskService.updateTaskStatus(taskId, request, null);

                        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
                        verify(statusHistoryRepository).save(any());
                }

                @Test
                @DisplayName("should throw exception for invalid transition")
                void shouldThrowException_whenInvalidTransition() {
                        testTask.setStatus("TODO");
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("DONE"); // Invalid transition (To-Do -> DONE is blocked by canTransitionTo)

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request, null))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Invalid status transition");
                }

                @Test
                @DisplayName("should auto-advance to PENDING_APPROVAL if required")
                void shouldAutoAdvanceToPendingApproval() {
                        testTask.setStatus("IN_PROGRESS");
                        testTask.setApprovalRequired(true);
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_REVIEW");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);
                        when(taskMapper.toTaskResponse(any())).thenReturn(TaskResponse.builder().status("PENDING_APPROVAL").build());

                        TaskResponse result = taskService.updateTaskStatus(taskId, request, null);

                        assertThat(result.getStatus()).isEqualTo("PENDING_APPROVAL");
                }

                @Test
                @DisplayName("should block submission if proof is required but missing")
                void shouldBlockSubmission_whenProofMissing() {
                        testTask.setStatus("IN_PROGRESS");
                        testTask.setProofRequired(true);
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_REVIEW");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(attachmentRepository.existsByTaskIdAndAttachmentPurpose(taskId, AttachmentPurpose.PROOF))
                                .thenReturn(false);

                        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request, null))
                                .isInstanceOf(ProofEnforcementException.class);
                }

                @Test
                @DisplayName("should allow submission if proof is required and exists")
                void shouldAllowSubmission_whenProofExists() {
                        testTask.setStatus("IN_PROGRESS");
                        testTask.setProofRequired(true);
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_REVIEW");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(attachmentRepository.existsByTaskIdAndAttachmentPurpose(taskId, AttachmentPurpose.PROOF))
                                .thenReturn(true);
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

                        taskService.updateTaskStatus(taskId, request, null);

                        verify(taskRepository).save(any(Task.class));
                }

                @Test
                @DisplayName("should enforce mandatory cancel reason")
                void shouldEnforceCancelReason() {
                        testTask.setStatus("TODO");
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("CANCELLED");
                        request.setReason(null); // Missing reason

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request, null))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("Reason is mandatory when cancelling a task");
                }
                @Test
                @DisplayName("should record late status and minutes when overdue")
                void shouldRecordLateStatus() {
                        testTask.setStatus("IN_PROGRESS");
                        testTask.setDueDate(LocalDateTime.now().minusHours(2));
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_REVIEW");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

                        taskService.updateTaskStatus(taskId, request, null);

                        assertThat(testTask.getIsLate()).isTrue();
                        assertThat(testTask.getLateByMinutes()).isGreaterThanOrEqualTo(120);
                }

                @Test
                @DisplayName("should record IP address in status history")
                void shouldRecordIpAddress() {
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("IN_PROGRESS");
                        var mockRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
                        when(mockRequest.getRemoteAddr()).thenReturn("192.168.1.1");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

                        taskService.updateTaskStatus(taskId, request, mockRequest);

                        verify(statusHistoryRepository).save(argThat(h -> "192.168.1.1".equals(h.getIpAddress())));
                }

                @Test
                @DisplayName("should auto-advance to PENDING_APPROVAL even if DONE is requested")
                void shouldAutoAdvanceToPendingApproval_fromDone() {
                        testTask.setStatus("IN_PROGRESS");
                        testTask.setApprovalRequired(true);
                        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                        request.setStatus("DONE"); // Safety net test

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);
                        when(taskMapper.toTaskResponse(any())).thenReturn(TaskResponse.builder().status("PENDING_APPROVAL").build());

                        TaskResponse result = taskService.updateTaskStatus(taskId, request, null);

                        assertThat(result.getStatus()).isEqualTo("PENDING_APPROVAL");
                        assertThat(testTask.getStatus()).isEqualTo("PENDING_APPROVAL");
                }
        }

        @Nested
        @DisplayName("approveRejectTests")
        class ApproveRejectTests {
                @Test
                @DisplayName("should approve task successfully")
                void shouldApproveTask() {
                        testTask.setStatus("PENDING_APPROVAL");
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

                        taskService.approveTask(taskId, currentUserId);

                        assertThat(testTask.getStatus()).isEqualTo("DONE");
                        assertThat(testTask.getCompletedAt()).isNotNull();
                }

                @Test
                @DisplayName("should reject task and mark proofs as rejected")
                void shouldRejectTask() {
                        testTask.setStatus("PENDING_APPROVAL");
                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

                        taskService.rejectTask(taskId, currentUserId, "Needs more detail");

                        assertThat(testTask.getStatus()).isEqualTo("IN_REVIEW");
                        verify(attachmentRepository).updatePurposeByTaskId(
                            taskId, AttachmentPurpose.PROOF, AttachmentPurpose.REJECTED_PROOF);
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
