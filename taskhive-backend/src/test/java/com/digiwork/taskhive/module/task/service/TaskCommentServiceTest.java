package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.TaskCommentRequest;
import com.digiwork.taskhive.module.task.dto.TaskCommentResponse;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskCommentService taskCommentService;

    private UUID taskId;
    private UUID currentUserId;
    private UUID employeeId;
    private Task testTask;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        testTask = Task.builder()
                .id(taskId)
                .title("Test Task")
                .assignedTo(employeeId)
                .isDeleted(false)
                .build();

        setSecurityContext(currentUserId, "ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(UUID userId, String role) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId, "user@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // addComment Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addComment")
    class AddCommentTests {

        @Test
        @DisplayName("should add comment successfully as ADMIN")
        void shouldAddCommentSuccessfully() {
            TaskCommentRequest request = new TaskCommentRequest();
            request.setContent("Great progress!");

            TaskComment savedComment = TaskComment.builder()
                    .id(UUID.randomUUID()).taskId(taskId)
                    .authorId(currentUserId).content("Great progress!").build();
            TaskCommentResponse expectedResponse = TaskCommentResponse.builder()
                    .id(savedComment.getId().toString()).content("Great progress!").build();

            when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
            when(commentRepository.save(any(TaskComment.class))).thenReturn(savedComment);
            when(taskMapper.toTaskCommentResponse(savedComment)).thenReturn(expectedResponse);

            TaskCommentResponse result = taskCommentService.addComment(taskId, request);

            assertThat(result.getContent()).isEqualTo("Great progress!");
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should deny access when EMPLOYEE is not assigned to the task")
        void shouldDenyAccess_whenEmployeeNotAssigned() {
            setSecurityContext(currentUserId, "EMPLOYEE");
            TaskCommentRequest request = new TaskCommentRequest();
            request.setContent("Comment");

            Employee otherEmployee = Employee.builder()
                    .id(UUID.randomUUID()).userId(currentUserId).build();

            when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
            when(employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId))
                    .thenReturn(Optional.of(otherEmployee));

            assertThatThrownBy(() -> taskCommentService.addComment(taskId, request))
                    .isInstanceOf(TaskAccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowException_whenTaskNotFound() {
            TaskCommentRequest request = new TaskCommentRequest();
            request.setContent("Comment");

            when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCommentService.addComment(taskId, request))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getComments Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getComments")
    class GetCommentsTests {

        @Test
        @DisplayName("should return paginated comments for a task")
        void shouldGetCommentsWithPagination() {
            TaskComment comment = TaskComment.builder()
                    .id(UUID.randomUUID()).taskId(taskId)
                    .authorId(currentUserId).content("Comment").build();
            Page<TaskComment> page = new PageImpl<>(List.of(comment));
            TaskCommentResponse response = TaskCommentResponse.builder()
                    .id(comment.getId().toString()).content("Comment").build();

            when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
            when(commentRepository.findByTaskIdOrderByCreatedAtDesc(eq(taskId), any(Pageable.class)))
                    .thenReturn(page);
            when(taskMapper.toTaskCommentResponse(comment)).thenReturn(response);

            PageResponse<TaskCommentResponse> result = taskCommentService.getComments(taskId, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }
}
