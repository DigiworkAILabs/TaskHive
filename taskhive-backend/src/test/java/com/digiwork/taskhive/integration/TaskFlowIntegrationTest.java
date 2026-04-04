package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.task.dto.CreateTaskRequest;
import com.digiwork.taskhive.module.task.dto.TaskApprovalRequest;
import com.digiwork.taskhive.module.task.dto.UpdateTaskStatusRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskFlowIntegrationTest extends BaseIntegrationTest {

    private static final String TASKS_URL    = "/api/v1/tasks";
    private static final String MY_TASKS_URL = "/api/v1/tasks/my-tasks";
    private static final String LOGIN_URL    = "/api/v1/auth/login";

    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String EMP1_EMAIL     = "emp1.task@test.com";
    private static final String EMP1_PASSWORD  = "Emp1Pass@1";
    // EMP2 is only created in tests that need cross-employee ownership checks
    private static final String EMP2_EMAIL     = "emp2.task@test.com";
    private static final String EMP2_PASSWORD  = "Emp2Pass@1";

    @Autowired private RoleRepository  roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UUID emp1EmployeeId;

    @BeforeEach
    void setUp() {
        emp1EmployeeId = createActiveEmployee(EMP1_EMAIL, EMP1_PASSWORD, "Task", "Employee1");
    }

    @AfterEach
    void cleanup() {
        cleanupAllTestData();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID createActiveEmployee(String email, String password, String first, String last) {
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();

        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(first).lastName(last)
                .status(UserStatus.ACTIVE)
                .failedAttempts(0).isDeleted(false)
                .build());

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId()).roleId(role.getId()).build());

        Employee emp = employeeRepository.save(Employee.builder()
                .userId(user.getId())
                .firstName(first).lastName(last)
                .email(email).status("ACTIVE")
                .build());

        return emp.getId();
    }

    private Cookie loginCookie(String email, String password) throws Exception {
        MvcResult result = performPost(LOGIN_URL, new LoginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn();
        Cookie c = result.getResponse().getCookie("accessToken");
        assertThat(c).isNotNull();
        return c;
    }

    private CreateTaskRequest buildCreateRequest(UUID assignedTo) {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Integration Test Task");
        req.setDescription("Test description");
        req.setPriority("MEDIUM");
        req.setDueDate(LocalDateTime.now().plusDays(7));
        req.setAssignedTo(assignedTo);
        req.setProofRequired(false);
        req.setApprovalRequired(false);
        return req;
    }

    private String createTaskAsAdmin(UUID assignedTo) throws Exception {
        Cookie adminCookie = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult result = performPostWithCookie(TASKS_URL, buildCreateRequest(assignedTo), adminCookie)
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();
    }

    private void patchStatus(String taskId, String newStatus, Cookie cookie) throws Exception {
        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest(newStatus, null, null), cookie)
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: POST /tasks should create task and return 201")
    void adminCreatesTask_Returns201() throws Exception {
        Cookie admin = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);

        performPostWithCookie(TASKS_URL, buildCreateRequest(emp1EmployeeId), admin)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TODO"));
    }

    @Test
    @DisplayName("admin: GET /tasks should return paginated task list")
    void adminListsTasks_Returns200() throws Exception {
        createTaskAsAdmin(emp1EmployeeId);
        Cookie admin = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);

        performGetWithCookie(TASKS_URL, admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("admin: DELETE /tasks/{id} soft-deletes and subsequent GET returns 404")
    void adminDeletesTask_Returns404OnGet() throws Exception {
        String taskId = createTaskAsAdmin(emp1EmployeeId);
        Cookie admin  = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);

        performDeleteWithCookie(TASKS_URL + "/" + taskId, admin)
                .andExpect(status().isOk());

        performGetWithCookie(TASKS_URL + "/" + taskId, admin)
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Employee sees assigned task in /my-tasks
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: GET /my-tasks should return tasks assigned to them")
    void employeeSeesOwnTasksInMyTasks() throws Exception {
        createTaskAsAdmin(emp1EmployeeId);
        Cookie emp1 = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        performGetWithCookie(MY_TASKS_URL, emp1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Full FSM chain
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("task FSM: TODO→IN_PROGRESS→IN_REVIEW then admin approves→DONE")
    void fullStatusTransitionChainWithApproval() throws Exception {
        Cookie admin = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie emp1  = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        CreateTaskRequest req = buildCreateRequest(emp1EmployeeId);
        req.setApprovalRequired(true);

        MvcResult created = performPostWithCookie(TASKS_URL, req, admin)
                .andExpect(status().isCreated()).andReturn();
        String taskId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asText();

        patchStatus(taskId, "IN_PROGRESS", emp1);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest("IN_REVIEW", null, null), emp1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        performPatchWithCookie(TASKS_URL + "/" + taskId + "/approve", admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"));
    }

    @Test
    @DisplayName("task FSM: admin rejects task → status reverts to IN_REVIEW")
    void adminRejectsTask_StatusReverts() throws Exception {
        Cookie admin = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie emp1  = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        CreateTaskRequest req = buildCreateRequest(emp1EmployeeId);
        req.setApprovalRequired(true);

        MvcResult created = performPostWithCookie(TASKS_URL, req, admin)
                .andExpect(status().isCreated()).andReturn();
        String taskId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asText();

        patchStatus(taskId, "IN_PROGRESS", emp1);
        patchStatus(taskId, "IN_REVIEW", emp1);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/reject",
                new TaskApprovalRequest("Work not satisfactory"), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_REVIEW"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Invalid FSM transition guard
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("task FSM: invalid transition TODO→DONE returns 400")
    void invalidStatusTransition_Returns400() throws Exception {
        String taskId = createTaskAsAdmin(emp1EmployeeId);
        Cookie emp1   = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest("DONE", null, null), emp1)
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Ownership enforcement
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: cannot update status of another employee's task → 403")
    void employee_CannotUpdateOtherEmployeeTask_Returns403() throws Exception {
        // Task assigned to emp1; emp2 tries to update it
        String taskId = createTaskAsAdmin(emp1EmployeeId);

        // Create emp2 only for this test
        createActiveEmployee(EMP2_EMAIL, EMP2_PASSWORD, "Task", "Employee2");
        Cookie emp2 = loginCookie(EMP2_EMAIL, EMP2_PASSWORD);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest("IN_PROGRESS", null, null), emp2)
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Employee cannot cancel a task
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: cannot cancel a task → 403")
    void employee_CannotCancelTask_Returns403() throws Exception {
        String taskId = createTaskAsAdmin(emp1EmployeeId);
        Cookie emp1   = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        patchStatus(taskId, "IN_PROGRESS", emp1);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest("CANCELLED", null, "Trying to cancel"), emp1)
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Mandatory cancel reason
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: cancel without reason → 400")
    void cancelWithoutReason_Returns400() throws Exception {
        String taskId = createTaskAsAdmin(emp1EmployeeId);
        Cookie admin  = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);

        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                new UpdateTaskStatusRequest("CANCELLED", null, null), admin)
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Role-based restrictions
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: GET /tasks (admin-only list) returns 403")
    void employee_CannotAccessAdminTaskList_Returns403() throws Exception {
        Cookie emp1 = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);
        performGetWithCookie(TASKS_URL, emp1)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee: DELETE /tasks/{id} returns 403")
    void employee_CannotDeleteTask_Returns403() throws Exception {
        String taskId = createTaskAsAdmin(emp1EmployeeId);
        Cookie emp1   = loginCookie(EMP1_EMAIL, EMP1_PASSWORD);

        performDeleteWithCookie(TASKS_URL + "/" + taskId, emp1)
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Unauthenticated
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("unauthenticated: GET /tasks returns 401")
    void unauthenticated_Returns401() throws Exception {
        performGet(TASKS_URL)
                .andExpect(status().isUnauthorized());
    }
}
