package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.TaskAttachmentResponse;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import com.digiwork.taskhive.module.task.repository.TaskAttachmentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAttachmentServiceTest {

        @Mock
        private TaskAttachmentRepository attachmentRepository;
        @Mock
        private TaskRepository taskRepository;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private StorageService storageService;
        @Mock
        private TaskMapper taskMapper;

        @InjectMocks
        private TaskAttachmentService taskAttachmentService;

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
        // uploadAttachment Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("uploadAttachment")
        class UploadAttachmentTests {

                @Test
                @DisplayName("should upload attachment successfully as ADMIN")
                void shouldUploadAttachmentSuccessfully() throws IOException {
                        MultipartFile file = mock(MultipartFile.class);
                        when(file.isEmpty()).thenReturn(false);
                        when(file.getSize()).thenReturn(1024L);
                        when(file.getContentType()).thenReturn("application/pdf");
                        when(file.getOriginalFilename()).thenReturn("doc.pdf");

                        TaskAttachment saved = TaskAttachment.builder().id(UUID.randomUUID()).taskId(taskId).build();
                        TaskAttachmentResponse expectedResponse = TaskAttachmentResponse.builder()
                                        .id(saved.getId().toString()).build();

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(storageService.store(eq(file), eq("tasks/attachments"), anyString()))
                                        .thenReturn("tasks/attachments/file");
                        when(attachmentRepository.save(any(TaskAttachment.class))).thenReturn(saved);
                        when(taskMapper.toTaskAttachmentResponse(saved)).thenReturn(expectedResponse);

                        TaskAttachmentResponse result = taskAttachmentService.uploadAttachment(taskId, file, "GENERAL");

                        assertThat(result).isEqualTo(expectedResponse);
                }

                @Test
                @DisplayName("should deny access when EMPLOYEE is not assigned to the task")
                void shouldDenyAccess_whenEmployeeNotAssigned() {
                        setSecurityContext(currentUserId, "EMPLOYEE");
                        MultipartFile file = mock(MultipartFile.class);
                        lenient().when(file.isEmpty()).thenReturn(false);
                        lenient().when(file.getSize()).thenReturn(1024L);
                        lenient().when(file.getContentType()).thenReturn("application/pdf");

                        Employee otherEmployee = Employee.builder()
                                        .id(UUID.randomUUID()).userId(currentUserId).build();

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId))
                                        .thenReturn(Optional.of(otherEmployee));

                        assertThatThrownBy(() -> taskAttachmentService.uploadAttachment(taskId, file, "GENERAL"))
                                        .isInstanceOf(TaskAccessDeniedException.class);
                }

                @Test
                @DisplayName("should throw TaskNotFoundException when task not found")
                void shouldThrowException_whenTaskNotFound() {
                        MultipartFile file = mock(MultipartFile.class);
                        lenient().when(file.isEmpty()).thenReturn(false);
                        lenient().when(file.getSize()).thenReturn(1024L);
                        lenient().when(file.getContentType()).thenReturn("application/pdf");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> taskAttachmentService.uploadAttachment(taskId, file, "GENERAL"))
                                        .isInstanceOf(TaskNotFoundException.class);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getAttachments Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("getAttachments")
        class GetAttachmentsTests {

                @Test
                @DisplayName("should return attachments for a task")
                void shouldListAttachmentsForTask() {
                        TaskAttachment attachment = TaskAttachment.builder()
                                        .id(UUID.randomUUID()).taskId(taskId).build();
                        TaskAttachmentResponse response = TaskAttachmentResponse.builder()
                                        .id(attachment.getId().toString()).build();

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));
                        when(attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId))
                                        .thenReturn(List.of(attachment));
                        when(taskMapper.toTaskAttachmentResponse(attachment)).thenReturn(response);

                        List<TaskAttachmentResponse> result = taskAttachmentService.getAttachments(taskId);

                        assertThat(result).hasSize(1);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // validateFile Tests
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("validateFile")
        class ValidateFileTests {

                @Test
                @DisplayName("should throw when file is empty")
                void shouldThrowException_whenFileEmpty() {
                        MultipartFile file = mock(MultipartFile.class);
                        when(file.isEmpty()).thenReturn(true);

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskAttachmentService.uploadAttachment(taskId, file, "GENERAL"))
                                        .isInstanceOf(BusinessException.class)
                                        .hasMessageContaining("File is required");
                }

                @Test
                @DisplayName("should throw when file exceeds 20MB")
                void shouldThrowException_whenFileTooLarge() {
                        MultipartFile file = mock(MultipartFile.class);
                        when(file.isEmpty()).thenReturn(false);
                        when(file.getSize()).thenReturn(21 * 1024 * 1024L);

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskAttachmentService.uploadAttachment(taskId, file, "GENERAL"))
                                        .isInstanceOf(BusinessException.class)
                                        .hasMessageContaining("20MB");
                }

                @Test
                @DisplayName("should throw when file type not allowed")
                void shouldThrowException_whenFileTypeNotAllowed() {
                        MultipartFile file = mock(MultipartFile.class);
                        when(file.isEmpty()).thenReturn(false);
                        when(file.getSize()).thenReturn(1024L);
                        when(file.getContentType()).thenReturn("application/x-executable");

                        when(taskRepository.findByIdAndIsDeletedFalse(taskId)).thenReturn(Optional.of(testTask));

                        assertThatThrownBy(() -> taskAttachmentService.uploadAttachment(taskId, file, "GENERAL"))
                                        .isInstanceOf(BusinessException.class)
                                        .hasMessageContaining("File type not allowed");
                }
        }
}
