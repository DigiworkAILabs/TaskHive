package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.service.TaskSearchService;
import com.digiwork.taskhive.module.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TaskControllerTest.SecurityTestConfig.class)
class TaskControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskSearchService taskSearchService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private AuditService auditService;

    private UsernamePasswordAuthenticationToken getAdminAuth() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "admin@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken getEmployeeAuth() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "employee@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("POST /api/v1/tasks")
    class CreateTaskTests {
        @Test
        @DisplayName("Admin can create task → 201 CREATED")
        void createTaskSuccess() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test Task");
            request.setPriority("MEDIUM");
            request.setDueDate(java.time.LocalDateTime.now().plusDays(1));
            request.setAssignedTo(UUID.randomUUID());

            TaskResponse response = new TaskResponse();
            response.setId(UUID.randomUUID().toString());
            response.setTitle("Test Task");

            when(taskService.createTask(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/tasks")
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Task created successfully"))
                    .andExpect(jsonPath("$.data.title").value("Test Task"));
        }

        @Test
        @DisplayName("Invalid task payload → 400 BAD REQUEST")
        void createTaskValidationFailure() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(""); // Invalid blank title

            mockMvc.perform(post("/api/v1/tasks")
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Employee cannot create task → 403 FORBIDDEN")
        void createTaskForbidden() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test Task");
            request.setPriority("MEDIUM");
            request.setDueDate(java.time.LocalDateTime.now().plusDays(1));
            request.setAssignedTo(UUID.randomUUID());

            mockMvc.perform(post("/api/v1/tasks")
                            .with(authentication(getEmployeeAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks")
    class GetAllTasksTests {
        @Test
        @DisplayName("Admin can get all tasks → 200 OK")
        void getAllTasksSuccess() throws Exception {
            TaskListResponse taskItem = TaskListResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("All Tasks Item")
                    .status("TODO")
                    .build();
            PageResponse<TaskListResponse> pageResponse = PageResponse.<TaskListResponse>builder()
                    .content(List.of(taskItem))
                    .page(0).size(10).totalElements(1).totalPages(1).last(true)
                    .build();

            when(taskService.getAllTasks(any(TaskFilterRequest.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/tasks")
                            .with(authentication(getAdminAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Tasks retrieved successfully"))
                    .andExpect(jsonPath("$.data.content[0].title").value("All Tasks Item"));
        }

        @Test
        @DisplayName("Employee cannot get all tasks → 403 FORBIDDEN")
        void getAllTasksForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/tasks")
                            .with(authentication(getEmployeeAuth())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/my-tasks")
    class GetMyTasksTests {
        @Test
        @DisplayName("Authenticated user can get own tasks → 200 OK")
        void getMyTasksSuccess() throws Exception {
            TaskListResponse myTask = TaskListResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("My Task")
                    .status("IN_PROGRESS")
                    .build();
            PageResponse<TaskListResponse> pageResponse = PageResponse.<TaskListResponse>builder()
                    .content(List.of(myTask))
                    .page(0).size(10).totalElements(1).totalPages(1).last(true)
                    .build();

            when(taskService.getMyTasks(anyInt(), anyInt(), anyString(), anyString(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/tasks/my-tasks")
                            .with(authentication(getEmployeeAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("My tasks retrieved successfully"))
                    .andExpect(jsonPath("$.data.content[0].title").value("My Task"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/search")
    class SearchTasksTests {
        @Test
        @DisplayName("Search tasks for employee → 200 OK")
        void searchTasksEmployee() throws Exception {
            UsernamePasswordAuthenticationToken auth = getEmployeeAuth();
            CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
            
            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            
            when(employeeRepository.findByUserIdAndIsDeletedFalse(principal.getId()))
                    .thenReturn(Optional.of(employee));
            when(taskSearchService.searchTasks(anyString(), any(), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>());

            mockMvc.perform(get("/api/v1/tasks/search")
                            .param("query", "test")
                            .with(authentication(auth)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/{id}")
    class GetTaskByIdTests {
        @Test
        @DisplayName("Get task by ID → 200 OK")
        void getTaskByIdSuccess() throws Exception {
            when(taskService.getTaskById(any())).thenReturn(new TaskResponse());

            mockMvc.perform(get("/api/v1/tasks/" + UUID.randomUUID())
                            .with(authentication(getEmployeeAuth())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tasks/{id}")
    class UpdateTaskTests {
        @Test
        @DisplayName("Admin can update task → 200 OK")
        void updateTaskSuccess() throws Exception {
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Title");

            TaskResponse updatedTask = TaskResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Updated Title")
                    .status("TODO")
                    .build();
            when(taskService.updateTask(any(), any())).thenReturn(updatedTask);

            mockMvc.perform(put("/api/v1/tasks/" + UUID.randomUUID())
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task updated successfully"))
                    .andExpect(jsonPath("$.data.title").value("Updated Title"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tasks/{id}")
    class DeleteTaskTests {
        @Test
        @DisplayName("Admin can delete task → 200 OK")
        void deleteTaskSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/tasks/" + UUID.randomUUID())
                            .with(authentication(getAdminAuth()))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{id}/status")
    class UpdateTaskStatusTests {
        @Test
        @DisplayName("Update task status → 200 OK")
        void updateTaskStatusSuccess() throws Exception {
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            request.setStatus("IN_PROGRESS");

            when(taskService.updateTaskStatus(any(), any(), any())).thenReturn(new TaskResponse());

            mockMvc.perform(patch("/api/v1/tasks/" + UUID.randomUUID() + "/status")
                            .with(authentication(getEmployeeAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{id}/approve")
    class ApproveTaskTests {
        @Test
        @DisplayName("Admin can approve task → 200 OK")
        void approveTaskSuccess() throws Exception {
            TaskResponse approvedTask = TaskResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Approved Task")
                    .status("DONE")
                    .build();
            when(taskService.approveTask(any(), any())).thenReturn(approvedTask);

            mockMvc.perform(patch("/api/v1/tasks/" + UUID.randomUUID() + "/approve")
                            .with(authentication(getAdminAuth()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task approved successfully"))
                    .andExpect(jsonPath("$.data.title").value("Approved Task"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{id}/reject")
    class RejectTaskTests {
        @Test
        @DisplayName("Admin can reject task → 200 OK")
        void rejectTaskSuccess() throws Exception {
            TaskApprovalRequest request = new TaskApprovalRequest();
            request.setReason("Incomplete");

            TaskResponse rejectedTask = TaskResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Rejected Task")
                    .status("IN_PROGRESS")
                    .build();
            when(taskService.rejectTask(any(), any(), any())).thenReturn(rejectedTask);

            mockMvc.perform(patch("/api/v1/tasks/" + UUID.randomUUID() + "/reject")
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task rejected and returned for revision"))
                    .andExpect(jsonPath("$.data.title").value("Rejected Task"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/late")
    class GetLateTasksTests {
        @Test
        @DisplayName("Admin can get late tasks → 200 OK")
        void getLateTasksSuccess() throws Exception {
            when(taskService.getLateTasks()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/tasks/late")
                            .with(authentication(getAdminAuth())))
                    .andExpect(status().isOk());
        }
    }
}
