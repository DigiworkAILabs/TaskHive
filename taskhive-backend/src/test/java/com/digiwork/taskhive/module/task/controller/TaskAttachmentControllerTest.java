package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.task.dto.TaskAttachmentResponse;
import com.digiwork.taskhive.module.task.dto.TaskStatusHistoryResponse;
import com.digiwork.taskhive.module.task.service.TaskAttachmentService;
import com.digiwork.taskhive.module.task.service.TaskStatusHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskAttachmentController.class, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
})
@Import(TaskAttachmentControllerTest.SecurityTestConfig.class)
class TaskAttachmentControllerTest {

        @TestConfiguration
        @EnableMethodSecurity
        static class SecurityTestConfig {
                @Bean
                public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
                        http.csrf(csrf -> csrf.disable())
                                        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                                        .exceptionHandling(ex -> ex.authenticationEntryPoint(
                                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
                        return http.build();
                }

                @Bean(name = "taskSecurity")
                public com.digiwork.taskhive.module.task.security.TaskSecurity taskSecurityMock() {
                        return org.mockito.Mockito.mock(com.digiwork.taskhive.module.task.security.TaskSecurity.class);
                }
        }

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private TaskAttachmentService attachmentService;

        @MockitoBean
        private TaskStatusHistoryService statusHistoryService;

        @MockitoBean
        private com.digiwork.taskhive.module.audit.service.AuditService auditService;

        @Autowired
        private com.digiwork.taskhive.module.task.security.TaskSecurity taskSecurity;

        @BeforeEach
        void setUp() {
                when(taskSecurity.canAccessTask(any())).thenReturn(true);
        }

        // ── Auth helpers ──────────────────────────────────────────────────────────

        private UsernamePasswordAuthenticationToken getAdminAuth() {
                CustomUserDetails userDetails = new CustomUserDetails(
                                UUID.randomUUID(), "admin@example.com", "password", true,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }

        private UsernamePasswordAuthenticationToken getEmployeeAuth() {
                CustomUserDetails userDetails = new CustomUserDetails(
                                UUID.randomUUID(), "employee@example.com", "password", true,
                                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }

        // ── POST /api/v1/tasks/{taskId}/attachments ───────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/tasks/{taskId}/attachments")
        class UploadAttachmentTests {

                @Test
                @DisplayName("Admin uploads valid file → 201 CREATED")
                void uploadAttachment_admin_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        TaskAttachmentResponse response = TaskAttachmentResponse.builder()
                                        .id(UUID.randomUUID().toString())
                                        .taskId(taskId.toString())
                                        .fileName("report.pdf")
                                        .fileSize(1024L)
                                        .mimeType("application/pdf")
                                        .attachmentPurpose("GENERAL")
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(attachmentService.uploadAttachment(any(), any(), anyString())).thenReturn(response);

                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "report.pdf", "application/pdf", "pdf content".getBytes());

                        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                                        .file(file)
                                        .param("purpose", "GENERAL")
                                        .with(authentication(getAdminAuth()))
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Attachment uploaded successfully"))
                                        .andExpect(jsonPath("$.data.fileName").value("report.pdf"));
                }

                @Test
                @DisplayName("Employee uploads proof attachment → 201 CREATED")
                void uploadAttachment_employee_proof_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        TaskAttachmentResponse response = TaskAttachmentResponse.builder()
                                        .id(UUID.randomUUID().toString())
                                        .taskId(taskId.toString())
                                        .fileName("proof.png")
                                        .attachmentPurpose("PROOF")
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(attachmentService.uploadAttachment(any(), any(), anyString())).thenReturn(response);

                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "proof.png", "image/png", new byte[] { 1, 2, 3 });

                        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                                        .file(file)
                                        .param("purpose", "PROOF")
                                        .with(authentication(getEmployeeAuth()))
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.data.attachmentPurpose").value("PROOF"));
                }

                @Test
                @DisplayName("Upload without purpose uses default GENERAL → 201 CREATED")
                void uploadAttachment_defaultPurpose() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        TaskAttachmentResponse response = TaskAttachmentResponse.builder()
                                        .id(UUID.randomUUID().toString())
                                        .taskId(taskId.toString())
                                        .fileName("doc.pdf")
                                        .attachmentPurpose("GENERAL")
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(attachmentService.uploadAttachment(any(), any(), anyString())).thenReturn(response);

                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "doc.pdf", "application/pdf", "content".getBytes());

                        // No purpose param — controller defaults to "GENERAL"
                        mockMvc.perform(multipart("/api/v1/tasks/" + taskId + "/attachments")
                                        .file(file)
                                        .with(authentication(getAdminAuth()))
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("Unauthenticated request → 401 UNAUTHORIZED")
                void uploadAttachment_unauthenticated_returns401() throws Exception {
                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "doc.pdf", "application/pdf", "content".getBytes());

                        mockMvc.perform(multipart("/api/v1/tasks/" + UUID.randomUUID() + "/attachments")
                                        .file(file)
                                        .with(csrf()))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ── GET /api/v1/tasks/{taskId}/attachments ────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/tasks/{taskId}/attachments")
        class GetAttachmentsTests {

                @Test
                @DisplayName("Admin gets attachment list → 200 OK")
                void getAttachments_admin_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        List<TaskAttachmentResponse> attachments = List.of(
                                        TaskAttachmentResponse.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .taskId(taskId.toString())
                                                        .fileName("file1.pdf")
                                                        .createdAt(LocalDateTime.now())
                                                        .build(),
                                        TaskAttachmentResponse.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .taskId(taskId.toString())
                                                        .fileName("file2.png")
                                                        .createdAt(LocalDateTime.now())
                                                        .build());

                        when(attachmentService.getAttachments(taskId)).thenReturn(attachments);

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/attachments")
                                        .with(authentication(getAdminAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Attachments retrieved successfully"))
                                        .andExpect(jsonPath("$.data.length()").value(2))
                                        .andExpect(jsonPath("$.data[0].fileName").value("file1.pdf"));
                }

                @Test
                @DisplayName("Empty attachment list → 200 OK with empty array")
                void getAttachments_emptyList() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        when(attachmentService.getAttachments(taskId)).thenReturn(List.of());

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/attachments")
                                        .with(authentication(getEmployeeAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.length()").value(0));
                }

                @Test
                @DisplayName("Unauthenticated request → 401 UNAUTHORIZED")
                void getAttachments_unauthenticated_returns401() throws Exception {
                        mockMvc.perform(get("/api/v1/tasks/" + UUID.randomUUID() + "/attachments"))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ── GET /api/v1/tasks/{taskId}/history ────────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/tasks/{taskId}/history")
        class GetStatusHistoryTests {

                @Test
                @DisplayName("Admin gets status history → 200 OK")
                void getStatusHistory_admin_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        List<TaskStatusHistoryResponse> history = List.of(
                                        TaskStatusHistoryResponse.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .taskId(taskId.toString())
                                                        .oldStatus("TODO")
                                                        .newStatus("IN_PROGRESS")
                                                        .changedByName("John Doe")
                                                        .changedAt(LocalDateTime.now())
                                                        .build());

                        when(statusHistoryService.getHistory(taskId)).thenReturn(history);

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/history")
                                        .with(authentication(getAdminAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Status history retrieved successfully"))
                                        .andExpect(jsonPath("$.data.length()").value(1))
                                        .andExpect(jsonPath("$.data[0].oldStatus").value("TODO"))
                                        .andExpect(jsonPath("$.data[0].newStatus").value("IN_PROGRESS"));
                }

                @Test
                @DisplayName("Employee gets history of their task → 200 OK")
                void getStatusHistory_employee_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        when(statusHistoryService.getHistory(taskId)).thenReturn(List.of());

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/history")
                                        .with(authentication(getEmployeeAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data").isArray());
                }

                @Test
                @DisplayName("Unauthenticated request → 401 UNAUTHORIZED")
                void getStatusHistory_unauthenticated_returns401() throws Exception {
                        mockMvc.perform(get("/api/v1/tasks/" + UUID.randomUUID() + "/history"))
                                        .andExpect(status().isUnauthorized());
                }
        }
}
