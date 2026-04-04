package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GdprFlowIntegrationTest
 * ─────────────────────────
 * Full HTTP integration tests for:
 *   GET  /api/v1/users/{userId}/data-export
 *   POST /api/v1/users/{userId}/gdpr-delete
 *
 * Covers:
 * - User exports their own data → 200
 * - User tries to export another user's data → 403
 * - Admin exports any user's data → 200
 * - User requests GDPR deletion → PII is anonymized in DB
 * - User tries to delete another user's data → 403
 */
class GdprFlowIntegrationTest extends BaseIntegrationTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private static final String EMP1_EMAIL    = "gdpr.emp1@test.com";
    private static final String EMP1_PASSWORD = "GdprPass@1";
    private static final String EMP2_EMAIL    = "gdpr.emp2@test.com";
    private static final String EMP2_PASSWORD = "GdprPass@2";

    @Autowired private RoleRepository      roleRepository;
    @Autowired private PasswordEncoder     passwordEncoder;

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Creates an active employee user and returns [userId, accessToken cookie].
     */
    private Object[] createActiveUserAndLogin(String email, String password) throws Exception {
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();

        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("GDPR")
                .lastName("User")
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
                .firstName("GDPR")
                .lastName("User")
                .email(email)
                .status("ACTIVE")
                .build());

        MvcResult result = performPost(LOGIN_URL, new LoginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = result.getResponse().getCookie("accessToken");
        assertThat(cookie).isNotNull();
        return new Object[]{ user.getId(), cookie };
    }

    private Cookie adminCookie() throws Exception {
        MvcResult r = performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk()).andReturn();
        return r.getResponse().getCookie("accessToken");
    }

    private String dataExportUrl(UUID userId) {
        return "/api/v1/users/" + userId + "/data-export";
    }

    private String gdprDeleteUrl(UUID userId) {
        return "/api/v1/users/" + userId + "/gdpr-delete";
    }

    @AfterEach
    void cleanup() {
        cleanupAllTestData();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Self-access
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("user: GET /users/{id}/data-export for own ID returns 200")
    void user_ExportsOwnData_Returns200() throws Exception {
        Object[] emp1 = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        UUID emp1UserId = (UUID) emp1[0];
        Cookie emp1Cookie = (Cookie) emp1[1];

        performGetWithCookie(dataExportUrl(emp1UserId), emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(EMP1_EMAIL));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Cross-user access blocked
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("user: GET /users/{otherId}/data-export returns 403")
    void user_CannotExportOtherUserData_Returns403() throws Exception {
        Object[] emp1 = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        Object[] emp2 = createActiveUserAndLogin(EMP2_EMAIL, EMP2_PASSWORD);

        UUID emp2UserId = (UUID) emp2[0];
        Cookie emp1Cookie = (Cookie) emp1[1];

        // emp1 tries to export emp2's data
        performGetWithCookie(dataExportUrl(emp2UserId), emp1Cookie)
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin bypass — admin can export any user's data
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /users/{anyId}/data-export returns 200")
    void admin_ExportsAnyUserData_Returns200() throws Exception {
        Object[] emp1 = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        UUID emp1UserId = (UUID) emp1[0];

        performGetWithCookie(dataExportUrl(emp1UserId), adminCookie())
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GDPR deletion anonymizes PII in DB
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("user: POST /users/{id}/gdpr-delete anonymizes PII in DB")
    void user_GdprDelete_AnonymizesPiiInDb() throws Exception {
        // FIX: verified GdprService uses REQUIRED propagation — test is safe as-is.
        Object[] emp1 = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        UUID emp1UserId = (UUID) emp1[0];
        Cookie emp1Cookie = (Cookie) emp1[1];

        performPostWithCookie(gdprDeleteUrl(emp1UserId), null, emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify the original email is gone from the DB
        assertThat(userRepository.findByEmailAndIsDeletedFalse(EMP1_EMAIL)).isEmpty();

        // Verify the user record is now deleted
        User anonymized = userRepository.findById(emp1UserId).orElseThrow();
        assertThat(anonymized.getFirstName()).isEqualTo("[DELETED]");
        assertThat(anonymized.getLastName()).isEqualTo("[DELETED]");
        assertThat(anonymized.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(anonymized.getIsDeleted()).isTrue();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Cross-user deletion blocked
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("user: POST /users/{otherId}/gdpr-delete returns 403")
    void user_CannotDeleteOtherUserData_Returns403() throws Exception {
        Object[] emp1 = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        Object[] emp2 = createActiveUserAndLogin(EMP2_EMAIL, EMP2_PASSWORD);

        UUID emp2UserId = (UUID) emp2[0];
        Cookie emp1Cookie = (Cookie) emp1[1];

        performPostWithCookie(gdprDeleteUrl(emp2UserId), null, emp1Cookie)
                .andExpect(status().isForbidden());
    }
}
