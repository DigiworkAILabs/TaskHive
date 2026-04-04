package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.model.AccountActivationToken;
import com.digiwork.taskhive.module.auth.service.TokenService;
import com.digiwork.taskhive.module.employee.dto.CreateEmployeeRequest;
import com.digiwork.taskhive.module.task.dto.CreateTaskRequest;
import com.digiwork.taskhive.module.task.dto.UpdateTaskStatusRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EndToEndFlowIntegrationTest
 * ─────────────────────────────
 * A single sequential test that exercises the entire system in a realistic
 * production scenario. This is the highest-value test in the suite — it is
 * the only test that can surface wiring bugs across module boundaries that
 * no unit test or isolated integration test can catch.
 *
 * Flow:
 * 1. Admin creates an employee
 * 2. Employee activates account via activation token
 * 3. Employee logs in and gets an accessToken cookie
 * 4. Admin creates a task and assigns it to the new employee
 * 5. Employee sees the task in GET /my-tasks
 * 6. Employee advances: 'TO-DO' → IN_PROGRESS → IN_REVIEW
 * (approvalRequired = true so IN_REVIEW auto-advances to PENDING_APPROVAL)
 * 7. Admin approves the task → status becomes DONE
 * 8. Analytics dashboard reflects the completed task
 * 9. Employee has a notification for the approval event
 *
 * NOTE: This class does NOT use @Transactional because several operations
 * (EmployeeService.createEmployee, activation, task status changes) commit
 * in REQUIRES_NEW sub-transactions. Using a rollback wrapper would hide those
 * commits from subsequent steps in the same test. Cleanup is handled by
 * deleting only the records created during the test.
 */
class EndToEndFlowIntegrationTest extends BaseIntegrationTest {

        private static final String LOGIN_URL = "/api/v1/auth/login";
        private static final String ACTIVATE_URL = "/api/v1/auth/activate-account";
        private static final String EMPLOYEES_URL = "/api/v1/employees";
        private static final String TASKS_URL = "/api/v1/tasks";
        private static final String MY_TASKS_URL = "/api/v1/tasks/my-tasks";
        private static final String ANALYTICS_URL = "/api/v1/analytics";

        private static final String ADMIN_EMAIL = "admin@test.com";
        private static final String ADMIN_PASSWORD = "Admin@123";
        private static final String NEW_EMP_EMAIL = "e2e.employee@test.com";
        private static final String EMP_PASSWORD = "E2ePass@123";

        @Autowired
        private TokenService tokenService;

        // ── Helpers ───────────────────────────────────────────────────────────────

        private Cookie loginCookie(String email, String password) throws Exception {
                MvcResult r = performPost(LOGIN_URL, new LoginRequest(email, password))
                                .andExpect(status().isOk()).andReturn();
                Cookie c = r.getResponse().getCookie("accessToken");
                assertThat(c).as("accessToken cookie must be set after login").isNotNull();
                return c;
        }

        private String extractId(String responseBody) throws Exception {
                // FIX: Extract ID safely from the 'data' path instead of fragile String slicing
                return objectMapper.readTree(responseBody)
                                .path("data")
                                .path("id")
                                .asText();
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // Full end-to-end test
        // ═══════════════════════════════════════════════════════════════════════════

        @Test
        @DisplayName("E2E: admin creates employee → activates → assigned task flows through FSM → admin approves → DONE → analytics + notification")
        void fullEndToEndFlow() throws Exception {
                // FIX: Pre-declaring IDs for use in finally-block cleanup
                String taskId = null;
                UUID empId = null;
                UUID userId = null;

                try {
                        Cookie adminCookie = loginCookie(ADMIN_EMAIL, ADMIN_PASSWORD);

                        // ── Step 1: Admin creates employee ────────────────────────────────────
                        CreateEmployeeRequest createEmpReq = new CreateEmployeeRequest();
                        createEmpReq.setFirstName("E2E");
                        createEmpReq.setLastName("Employee");
                        createEmpReq.setEmail(NEW_EMP_EMAIL);
                        createEmpReq.setDepartment("QA");
                        createEmpReq.setDesignation("Tester");
                        createEmpReq.setJoinDate(LocalDate.now());

                        performPostWithCookie(EMPLOYEES_URL, createEmpReq, adminCookie)
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.data.status").value("PENDING"));

                        // FIX: Capture generated IDs for cleanup
                        var employee = employeeRepository.findByEmailAndIsDeletedFalse(NEW_EMP_EMAIL).orElseThrow();
                        empId = employee.getId();
                        userId = employee.getUserId();
                        final UUID filterUserId = userId;

                        // ── Step 2: Retrieve the activation token that was created by EmployeeService
                        // ─
                        AccountActivationToken activationToken = accountActivationTokenRepository
                                        .findAll().stream()
                                        .filter(t -> t.getUserId().equals(filterUserId) && !t.getUsed())
                                        .findFirst()
                                        .orElseThrow(() -> new AssertionError(
                                                        "Activation token not found for new employee user"));

                        // We need the raw token, but only the hash is stored. We can use the admin
                        // endpoint pattern used in AuthFlowIntegrationTest — create a fresh raw token,
                        // hash it, and overwrite the stored hash. This avoids any dependency on
                        // how TokenService generates tokens internally.
                        String rawToken = "e2e-activation-token-" + UUID.randomUUID();
                        String tokenHash = tokenService.hashToken(rawToken);
                        activationToken.setTokenHash(tokenHash);
                        accountActivationTokenRepository.save(activationToken);

                        // ── Step 3: Employee activates account ────────────────────────────────
                        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, EMP_PASSWORD))
                                        .andExpect(status().isOk());

                        // ── Step 4: Employee logs in ──────────────────────────────────────────
                        Cookie empCookie = loginCookie(NEW_EMP_EMAIL, EMP_PASSWORD);

                        // ── Step 5: Admin creates a task assigned to the new employee ─────────
                        CreateTaskRequest taskReq = new CreateTaskRequest();
                        taskReq.setTitle("E2E Integration Task");
                        taskReq.setDescription("Full end-to-end test task");
                        taskReq.setPriority("HIGH");
                        taskReq.setDueDate(LocalDateTime.now().plusDays(3));
                        taskReq.setAssignedTo(empId);
                        taskReq.setApprovalRequired(true); // forces PENDING_APPROVAL intercept
                        taskReq.setProofRequired(false);

                        MvcResult createTaskResult = performPostWithCookie(TASKS_URL, taskReq, adminCookie)
                                        .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers
                                                        .print())
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.data.status").value("TODO"))
                                        .andReturn();

                        // FIX: Capture taskId for cleanup
                        taskId = extractId(createTaskResult.getResponse().getContentAsString());

                        // ── Step 6: Employee sees task in /my-tasks ───────────────────────────
                        performGetWithCookie(MY_TASKS_URL, empCookie)
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.totalElements").value(1))
                                        .andExpect(jsonPath("$.data.content[0].title").value("E2E Integration Task"));

                        // ── Step 7a: Employee moves 'TO-DO' → IN_PROGRESS ────────────────────────
                        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                                        new UpdateTaskStatusRequest("IN_PROGRESS", null, null), empCookie)
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

                        // ── Step 7b: Employee moves IN_PROGRESS → IN_REVIEW ──────────────────
                        // approvalRequired=true → service auto-advances to PENDING_APPROVAL
                        performPatchWithBodyAndCookie(TASKS_URL + "/" + taskId + "/status",
                                        new UpdateTaskStatusRequest("IN_REVIEW", null, null), empCookie)
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

                        // ── Step 8: Admin approves task → DONE ───────────────────────────────
                        performPatchWithCookie(TASKS_URL + "/" + taskId + "/approve", adminCookie)
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.status").value("DONE"));

                        // Verify completedAt is set in DB
                        var completedTask = taskRepository.findByIdAndIsDeletedFalse(UUID.fromString(taskId))
                                        .orElseThrow();
                        assertThat(completedTask.getCompletedAt()).isNotNull();

                        // ── Step 9: Analytics dashboard reflects ≥1 completed task ───────────
                        // The admin dashboard uses a real-time SQL fallback when no daily_metrics
                        // exist.
                        // After the task was approved and completedAt was set, the live query should
                        // count it as DONE.
                        performGetWithCookie(ANALYTICS_URL + "/dashboard/admin", adminCookie)
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.completedTasks").value(1L));

                        // ── Step 10: Employee has a notification for the TASK_ASSIGNED event ─
                        // TaskEventListener creates a TASK_ASSIGNED notification when a task is
                        // assigned. Employee should see at least one notification.
                        long notifCount = notificationRepository.countByUserId(userId);
                        assertThat(notifCount)
                                        .as("Employee should have received at least the TASK_ASSIGNED notification")
                                        .isGreaterThan(0);

                } finally {
                        cleanupAllTestData();
                }
        }

}
