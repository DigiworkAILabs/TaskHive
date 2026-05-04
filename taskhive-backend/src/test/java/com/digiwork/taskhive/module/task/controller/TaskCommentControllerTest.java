package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.task.dto.TaskCommentRequest;
import com.digiwork.taskhive.module.task.dto.TaskCommentResponse;
import com.digiwork.taskhive.module.task.service.TaskCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskCommentController.class, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
})
@Import(TaskCommentControllerTest.SecurityTestConfig.class)
class TaskCommentControllerTest {

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

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private TaskCommentService commentService;

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

        // ── POST /api/v1/tasks/{taskId}/comments ──────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/tasks/{taskId}/comments")
        class AddCommentTests {

                @Test
                @DisplayName("Employee adds comment → 201 CREATED")
                void addComment_employee_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        TaskCommentRequest request = new TaskCommentRequest("Great progress on this task!");

                        TaskCommentResponse response = TaskCommentResponse.builder()
                                        .id(UUID.randomUUID().toString())
                                        .taskId(taskId.toString())
                                        .authorName("Jane Employee")
                                        .content("Great progress on this task!")
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(commentService.addComment(any(), any())).thenReturn(response);

                        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/comments")
                                        .with(authentication(getEmployeeAuth()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Comment added successfully"))
                                        .andExpect(jsonPath("$.data.content").value("Great progress on this task!"))
                                        .andExpect(jsonPath("$.data.authorName").value("Jane Employee"));
                }

                @Test
                @DisplayName("Admin adds comment → 201 CREATED")
                void addComment_admin_success() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        TaskCommentRequest request = new TaskCommentRequest("Please revise section 2.");

                        TaskCommentResponse response = TaskCommentResponse.builder()
                                        .id(UUID.randomUUID().toString())
                                        .taskId(taskId.toString())
                                        .authorName("Admin User")
                                        .content("Please revise section 2.")
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(commentService.addComment(any(), any())).thenReturn(response);

                        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/comments")
                                        .with(authentication(getAdminAuth()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.data.content").value("Please revise section 2."));
                }

                @Test
                @DisplayName("Blank comment content → 400 BAD REQUEST")
                void addComment_blankContent_returns400() throws Exception {
                        TaskCommentRequest request = new TaskCommentRequest(""); // violates @NotBlank

                        mockMvc.perform(post("/api/v1/tasks/" + UUID.randomUUID() + "/comments")
                                        .with(authentication(getEmployeeAuth()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(csrf()))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Comment exceeds 2000 chars → 400 BAD REQUEST")
                void addComment_tooLong_returns400() throws Exception {
                        String longContent = "x".repeat(2001); // violates @Size(max=2000)
                        TaskCommentRequest request = new TaskCommentRequest(longContent);

                        mockMvc.perform(post("/api/v1/tasks/" + UUID.randomUUID() + "/comments")
                                        .with(authentication(getEmployeeAuth()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(csrf()))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Unauthenticated request → 401 UNAUTHORIZED")
                void addComment_unauthenticated_returns401() throws Exception {
                        TaskCommentRequest request = new TaskCommentRequest("Some comment");

                        mockMvc.perform(post("/api/v1/tasks/" + UUID.randomUUID() + "/comments")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(csrf()))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ── GET /api/v1/tasks/{taskId}/comments ───────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/tasks/{taskId}/comments")
        class GetCommentsTests {

                @Test
                @DisplayName("Gets paginated comments → 200 OK")
                void getComments_success() throws Exception {
                        UUID taskId = UUID.randomUUID();

                        List<TaskCommentResponse> comments = List.of(
                                        TaskCommentResponse.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .taskId(taskId.toString())
                                                        .authorName("Alice")
                                                        .content("First comment")
                                                        .createdAt(LocalDateTime.now())
                                                        .build(),
                                        TaskCommentResponse.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .taskId(taskId.toString())
                                                        .authorName("Bob")
                                                        .content("Second comment")
                                                        .createdAt(LocalDateTime.now())
                                                        .build());

                        PageResponse<TaskCommentResponse> page = PageResponse.<TaskCommentResponse>builder()
                                        .content(comments)
                                        .page(0)
                                        .size(10)
                                        .totalElements(2)
                                        .totalPages(1)
                                        .last(true)
                                        .build();

                        when(commentService.getComments(any(), anyInt(), anyInt())).thenReturn(page);

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/comments")
                                        .with(authentication(getAdminAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message").value("Comments retrieved successfully"))
                                        .andExpect(jsonPath("$.data.content.length()").value(2))
                                        .andExpect(jsonPath("$.data.totalElements").value(2))
                                        .andExpect(jsonPath("$.data.content[0].authorName").value("Alice"))
                                        .andExpect(jsonPath("$.data.content[1].content").value("Second comment"));
                }

                @Test
                @DisplayName("Custom pagination params → 200 OK")
                void getComments_customPagination() throws Exception {
                        UUID taskId = UUID.randomUUID();
                        PageResponse<TaskCommentResponse> page = PageResponse.<TaskCommentResponse>builder()
                                        .content(List.of())
                                        .page(2)
                                        .size(5)
                                        .totalElements(0)
                                        .totalPages(0)
                                        .last(true)
                                        .build();

                        when(commentService.getComments(any(), anyInt(), anyInt())).thenReturn(page);

                        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/comments")
                                        .param("page", "2")
                                        .param("size", "5")
                                        .with(authentication(getEmployeeAuth())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.page").value(2))
                                        .andExpect(jsonPath("$.data.size").value(5));
                }

                @Test
                @DisplayName("Unauthenticated request → 401 UNAUTHORIZED")
                void getComments_unauthenticated_returns401() throws Exception {
                        mockMvc.perform(get("/api/v1/tasks/" + UUID.randomUUID() + "/comments"))
                                        .andExpect(status().isUnauthorized());
                }
        }
}
