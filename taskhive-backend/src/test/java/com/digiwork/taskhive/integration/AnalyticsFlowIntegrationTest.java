package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AnalyticsFlowIntegrationTest
 *
 * Root cause of the 403s: The admin cookie was obtained by calling
 * performPost(LOGIN_URL, ...) which fires a real HTTP request. That request
 * commits a refresh_token row, sets cookies on the response, and the
 * JwtAuthenticationFilter on the NEXT request reads the accessToken cookie,
 * calls loadUserByUsername, and grants ROLE_ADMIN via user_roles. This is
 * exactly correct — no change needed to the cookie-acquisition logic.
 *
 * The real 403 cause was that in the previous run the @AfterEach cleanup was
 * failing (security_events FK), which left stale rows from the previous test.
 * The next @BeforeEach then collided on the email unique constraint, the
 * setUp threw, and all test methods in the class failed before even running.
 * With the FK-complete cleanupAllTestData() the setup now succeeds cleanly.
 */
class AnalyticsFlowIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL  = "/api/v1/analytics";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String EMP_EMAIL      = "analytics.emp@test.com";
    private static final String EMP_PASSWORD   = "AnalyticsEmp@1";

    @Autowired private RoleRepository  roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Cookie adminCookie;
    private Cookie empCookie;

    @BeforeEach
    void setUp() throws Exception {
        // Admin login — admin is seeded at startup (committed), so this always works.
        MvcResult adminResult = performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk()).andReturn();
        adminCookie = adminResult.getResponse().getCookie("accessToken");
        assertThat(adminCookie).isNotNull();

        // Create an employee and login
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        User user = userRepository.save(User.builder()
                .email(EMP_EMAIL)
                .passwordHash(passwordEncoder.encode(EMP_PASSWORD))
                .firstName("Analytics").lastName("Emp")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0).isDeleted(false)
                .build());
        userRoleRepository.save(UserRole.builder()
                .userId(user.getId()).roleId(role.getId()).build());
        employeeRepository.save(Employee.builder()
                .userId(user.getId())
                .firstName("Analytics").lastName("Emp")
                .email(EMP_EMAIL).status("ACTIVE")
                .build());

        MvcResult empResult = performPost(LOGIN_URL, new LoginRequest(EMP_EMAIL, EMP_PASSWORD))
                .andExpect(status().isOk()).andReturn();
        empCookie = empResult.getResponse().getCookie("accessToken");
        assertThat(empCookie).isNotNull();
    }

    @AfterEach
    void cleanup() {
        cleanupAllTestData();
    }

    // ── Admin dashboards ──────────────────────────────────────────────────────

    @Test
    @DisplayName("admin: GET /analytics/dashboard/admin returns 200")
    void admin_AdminDashboard_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/dashboard/admin", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricDate").isNotEmpty());
    }

    @Test
    @DisplayName("admin: GET /analytics/dashboard/employee returns 200")
    void admin_EmployeeDashboard_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/dashboard/employee", adminCookie)
                .andExpect(status().isOk());
    }

    // ── Employee dashboard ────────────────────────────────────────────────────

    @Test
    @DisplayName("employee: GET /analytics/dashboard/employee returns 200")
    void employee_EmployeeDashboard_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/dashboard/employee", empCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── RBAC — employee blocked from admin endpoints ──────────────────────────

    @Test
    @DisplayName("employee: GET /analytics/dashboard/admin returns 403")
    void employee_CannotAccessAdminDashboard_Returns403() throws Exception {
        performGetWithCookie(BASE_URL + "/dashboard/admin", empCookie)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee: GET /analytics/tasks/distribution returns 403")
    void employee_CannotAccessTaskDistribution_Returns403() throws Exception {
        performGetWithCookie(BASE_URL + "/tasks/distribution", empCookie)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee: GET /analytics/employees/performance returns 403")
    void employee_CannotAccessPerformance_Returns403() throws Exception {
        performGetWithCookie(BASE_URL + "/employees/performance", empCookie)
                .andExpect(status().isForbidden());
    }

    // ── Admin analytics endpoints ─────────────────────────────────────────────

    @Test
    @DisplayName("admin: GET /analytics/tasks/distribution returns 200")
    void admin_TaskDistribution_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/tasks/distribution", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("admin: GET /analytics/tasks/by-priority returns 200")
    void admin_TasksByPriority_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/tasks/by-priority", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("admin: GET /analytics/tasks/completion-trend returns 200")
    void admin_CompletionTrend_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/tasks/completion-trend", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("admin: GET /analytics/employees/performance returns 200")
    void admin_EmployeePerformance_Returns200() throws Exception {
        performGetWithCookie(BASE_URL + "/employees/performance", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── Unauthenticated ───────────────────────────────────────────────────────

    @Test
    @DisplayName("unauthenticated: GET /analytics/dashboard/admin returns 401")
    void unauthenticated_AdminDashboard_Returns401() throws Exception {
        performGet(BASE_URL + "/dashboard/admin")
                .andExpect(status().isUnauthorized());
    }
}
