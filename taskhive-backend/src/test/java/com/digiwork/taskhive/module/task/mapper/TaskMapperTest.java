package com.digiwork.taskhive.module.task.mapper;

import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMapperTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private TaskMapper taskMapper;

    private UUID taskId;
    private UUID employeeId;
    private UUID userId;
    private Task task;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .status("TODO")
                .priority("HIGH")
                .assignedTo(employeeId)
                .dueDate(LocalDateTime.now())
                .lateByMinutes(0)
                .submittedAt(null)
                .proofRequired(true)
                .approvalRequired(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }

    @Test
    @DisplayName("toTaskResponse: should map all task fields")
    void toTaskResponse() {
        Employee employee = Employee.builder().id(employeeId).firstName("John").lastName("Doe").build();
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(employee));

        TaskResponse response = taskMapper.toTaskResponse(task);

        assertThat(response.getId()).isEqualTo(taskId.toString());
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getAssigneeName()).isEqualTo("John Doe");
        assertThat(response.getIsLate()).isFalse();
        assertThat(response.getProofRequired()).isTrue();
    }

    @Test
    @DisplayName("toTaskListResponse: should map required fields")
    void toTaskListResponse() {
        Employee employee = Employee.builder().id(employeeId).firstName("John").lastName("Doe").build();
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(employee));

        TaskListResponse response = taskMapper.toTaskListResponse(task);

        assertThat(response.getId()).isEqualTo(taskId.toString());
        assertThat(response.getAssigneeName()).isEqualTo("John Doe");
        assertThat(response.getIsLate()).isFalse();
    }

    @Test
    @DisplayName("toTaskCommentResponse: should map comment fields")
    void toTaskCommentResponse() {
        UUID commentId = UUID.randomUUID();
        TaskComment comment = TaskComment.builder()
                .id(commentId)
                .taskId(taskId)
                .authorId(userId)
                .content("Test Comment")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder().id(userId).firstName("Jane").lastName("Smith").build();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        TaskCommentResponse response = taskMapper.toTaskCommentResponse(comment);

        assertThat(response.getId()).isEqualTo(commentId.toString());
        assertThat(response.getAuthorName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("toTaskAttachmentResponse: should map attachment fields")
    void toTaskAttachmentResponse() {
        UUID attachmentId = UUID.randomUUID();
        TaskAttachment attachment = TaskAttachment.builder()
                .id(attachmentId)
                .taskId(taskId)
                .uploadedBy(userId)
                .fileName("test.png")
                .fileUrl("uploads/test.png")
                .fileSize(1024L)
                .mimeType("image/png")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder().id(userId).firstName("Jane").lastName("Smith").build();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(storageService.getUrl("uploads/test.png")).thenReturn("http://localhost:8080/uploads/test.png");

        TaskAttachmentResponse response = taskMapper.toTaskAttachmentResponse(attachment);

        assertThat(response.getId()).isEqualTo(attachmentId.toString());
        assertThat(response.getFileUrl()).isEqualTo("http://localhost:8080/uploads/test.png");
    }

    @Test
    @DisplayName("toTaskStatusHistoryResponse: should map history fields")
    void toTaskStatusHistoryResponse() {
        UUID historyId = UUID.randomUUID();
        TaskStatusHistory history = TaskStatusHistory.builder()
                .id(historyId)
                .taskId(taskId)
                .oldStatus("TODO")
                .newStatus("IN_PROGRESS")
                .changedBy(userId)
                .comment("Started task")
                .ipAddress("127.0.0.1")
                .changedAt(LocalDateTime.now())
                .build();

        User user = User.builder().id(userId).firstName("Jane").lastName("Smith").build();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        TaskStatusHistoryResponse response = taskMapper.toTaskStatusHistoryResponse(history);

        assertThat(response.getChangedByName()).isEqualTo("Jane Smith");
        assertThat(response.getIpAddress()).isEqualTo("127.0.0.1");
    }
}
