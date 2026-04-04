package com.digiwork.taskhive.integration;
 
import com.digiwork.taskhive.module.audit.model.AuditLog;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuditFlowIntegrationTest
 * ─────────────────────────
 * Full HTTP integration tests for /api/v1/audit.
 *
 * Covers:
 * - After login, an audit log entry exists in DB (event listener writes to real DB)
 * - Admin queries GET /audit/logs → returns entries
 * - Admin searches audit logs with filters (GET /audit/logs/search)
 * - Admin queries logs by entity (GET /audit/logs/entity/{type}/{id})
 * - Employee tries to access audit endpoints → 403
 * - Admin retrieves security events (GET /audit/security-events)
 * - Unauthenticated access → 401
 */
// Remove @Transactional from class level — AuditEventListener uses REQUIRES_NEW
// that needs cross-transaction visibility for the test counts to resolve correctly
class AuditFlowIntegrationTest extends BaseIntegrationTest {

    private static final String AUDIT_URL  = "/api/v1/audit";
    private static final String LOGIN_URL  = "/api/v1/auth/login";

    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private static final String EMP_EMAIL    = "audit.emp@test.com";
    private static final String EMP_PASSWORD = "AuditEmp@1";

    @Autowired private RoleRepository     roleRepository;
    @Autowired private AuditService       auditService;
    @Autowired private PasswordEncoder    passwordEncoder;

    private Cookie adminCookie;
    private Cookie empCookie;

    @BeforeEach
    void setUp() throws Exception {
        // Admin
        MvcResult adminResult = performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk()).andReturn();
        adminCookie = adminResult.getResponse().getCookie("accessToken");
        assertThat(adminCookie).isNotNull();

        // Employee
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        User user = userRepository.save(User.builder()
                .email(EMP_EMAIL)
                .passwordHash(passwordEncoder.encode(EMP_PASSWORD))
                .firstName("Audit")
                .lastName("Emp")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .isDeleted(false)
                .build());
        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build());
        employeeRepository.save(Employee.builder()
                .userId(user.getId())
                .firstName("Audit")
                .lastName("Emp")
                .email(EMP_EMAIL)
                .status("ACTIVE")
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

    // ═══════════════════════════════════════════════════════════════════════════
    // Audit log created on login (event listener writes to DB)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("login event: audit log entry is written to DB after successful login")
    void login_WritesAuditLogToDb() {
        // The @BeforeEach already performed a login for the admin.
        // AuditEventListener listens to UserAuthenticatedEvent and writes LOGIN entry.
        // Because AuditService.logAction uses a separate @Transactional(REQUIRES_NEW),
        // the write commits even inside our outer @Transactional test.
        // FIX: Awaitility tolerates async event listener writes. Resolves instantly if synchronous.
        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(auditLogRepository.count()).isGreaterThan(0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin queries GET /audit/logs
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /audit/logs returns 200 with entries")
    void admin_GetAuditLogs_Returns200() throws Exception {
        // Seed an audit entry so there is definitely at least one record.
        auditService.logAction(AuditLog.builder()
                .action("TEST_ACTION")
                .entityType("TASK")
                .afterState("{\"test\":true}")
                .build());

        performGetWithCookie(AUDIT_URL + "/logs", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin searches with filters
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /audit/logs/search with action filter returns matching entries")
    void admin_SearchAuditLogs_WithActionFilter() throws Exception {
        auditService.logAction(AuditLog.builder()
                .action("SEARCH_TEST_ACTION")
                .entityType("TASK")
                .afterState("{}")
                .build());

        performGetWithCookie(AUDIT_URL + "/logs/search?action=SEARCH_TEST_ACTION", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("admin: GET /audit/logs/search with entityType filter returns 200")
    void admin_SearchAuditLogs_WithEntityTypeFilter() throws Exception {
        performGetWithCookie(AUDIT_URL + "/logs/search?entityType=TASK", adminCookie)
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin queries by entity
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /audit/logs/entity/{type}/{id} returns timeline")
    void admin_GetEntityTimeline_Returns200() throws Exception {
        java.util.UUID entityId = java.util.UUID.randomUUID();
        auditService.logAction(AuditLog.builder()
                .action("ENTITY_TEST")
                .entityType("TASK")
                .entityId(entityId)
                .afterState("{}")
                .build());

        performGetWithCookie(AUDIT_URL + "/logs/entity/TASK/" + entityId, adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin retrieves security events
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /audit/security-events returns 200")
    void admin_GetSecurityEvents_Returns200() throws Exception {
        performGetWithCookie(AUDIT_URL + "/security-events", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RBAC — employee cannot access any audit endpoint
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: GET /audit/logs returns 403")
    void employee_CannotAccessAuditLogs_Returns403() throws Exception {
        performGetWithCookie(AUDIT_URL + "/logs", empCookie)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee: GET /audit/security-events returns 403")
    void employee_CannotAccessSecurityEvents_Returns403() throws Exception {
        performGetWithCookie(AUDIT_URL + "/security-events", empCookie)
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Unauthenticated access
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("unauthenticated: GET /audit/logs returns 401")
    void unauthenticated_Returns401() throws Exception {
        performGet(AUDIT_URL + "/logs")
                .andExpect(status().isUnauthorized());
    }
}
