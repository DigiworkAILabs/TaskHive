package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.notification.dto.NotificationPreferenceRequest;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.model.Notification;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NotificationFlowIntegrationTest
 * ─────────────────────────────────
 * Full HTTP integration tests for /api/v1/notifications.
 *
 * Covers:
 * - Employee polls GET /notifications and sees their own notifications
 * - Mark single notification as read (PATCH /{id}/read)
 * - Mark all read (PATCH /mark-all-read)
 * - Unread count decrements correctly after marking read
 * - Employee updates notification preferences (PUT /preferences)
 * - Employee cannot access another user's notification (403 on mark-read)
 * - Unauthenticated access → 401
 */
class NotificationFlowIntegrationTest extends BaseIntegrationTest {

    private static final String NOTIF_URL  = "/api/v1/notifications";
    private static final String LOGIN_URL  = "/api/v1/auth/login";

    private static final String EMP1_EMAIL    = "notif.emp1@test.com";
    private static final String EMP1_PASSWORD = "NotifEmp@1";
    private static final String EMP2_EMAIL    = "notif.emp2@test.com";
    private static final String EMP2_PASSWORD = "NotifEmp@2";

    @Autowired private RoleRepository          roleRepository;
    @Autowired private PasswordEncoder         passwordEncoder;

    private UUID    emp1UserId;
    private UUID    emp2UserId;
    private Cookie  emp1Cookie;

    @BeforeEach
    void setUp() throws Exception {
        Object[] emp1Data = createActiveUserAndLogin(EMP1_EMAIL, EMP1_PASSWORD);
        Object[] emp2Data = createActiveUserAndLogin(EMP2_EMAIL, EMP2_PASSWORD);
        emp1UserId = (UUID) emp1Data[0];
        emp2UserId = (UUID) emp2Data[0];
        emp1Cookie = (Cookie) emp1Data[1];
    }

    @AfterEach
    void cleanup() {
        cleanupAllTestData();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Object[] createActiveUserAndLogin(String email, String password) throws Exception {
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("Notif")
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
                .firstName("Notif")
                .lastName("User")
                .email(email)
                .status("ACTIVE")
                .build());
        MvcResult result = performPost(LOGIN_URL, new LoginRequest(email, password))
                .andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("accessToken");
        assertThat(cookie).isNotNull();
        return new Object[]{ user.getId(), cookie };
    }

    /** Directly inserts an unread notification for a user (bypasses service event chain). */
    private Notification seedNotification(UUID userId) {
        return notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(NotificationType.TASK_ASSIGNED)
                .title("Test notification")
                .message("You have been assigned a task")
                .isRead(false)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET /notifications — scoped to current user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: GET /notifications returns only their own notifications")
    void employee_GetNotifications_ReturnsOwnOnly() throws Exception {
        seedNotification(emp1UserId);
        seedNotification(emp1UserId);
        seedNotification(emp2UserId); // emp2's notification — should NOT appear for emp1

        MvcResult result = performGetWithCookie(NOTIF_URL, emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.totalElements").value(2))
                .andReturn();

        // Verify emp2's notification is not leaked
        assertThat(result.getResponse().getContentAsString()).doesNotContain(emp2UserId.toString());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Mark single as read
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: PATCH /{id}/read marks notification as read")
    void employee_MarkSingleAsRead_UpdatesReadFlag() throws Exception {
        Notification notif = seedNotification(emp1UserId);

        performPatchWithCookie(NOTIF_URL + "/" + notif.getId() + "/read", emp1Cookie)
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notif.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
        assertThat(updated.getReadAt()).isNotNull();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Mark all read
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: PATCH /mark-all-read clears all unread for current user")
    void employee_MarkAllRead_ClearsUnread() throws Exception {
        seedNotification(emp1UserId);
        seedNotification(emp1UserId);
        seedNotification(emp1UserId);

        performPatchWithCookie(NOTIF_URL + "/mark-all-read", emp1Cookie)
                .andExpect(status().isOk());

        long unread = notificationRepository.countByUserIdAndIsReadFalse(emp1UserId);
        assertThat(unread).isZero();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Unread count decrements after marking read
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("unread count decrements correctly after marking one notification read")
    void unreadCountDecrementsAfterMarkRead() throws Exception {
        Notification n1 = seedNotification(emp1UserId);
        seedNotification(emp1UserId); // second notification stays unread

        // Before: count should be 2
        performGetWithCookie(NOTIF_URL + "/unread-count", emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));

        // Mark one as read
        performPatchWithCookie(NOTIF_URL + "/" + n1.getId() + "/read", emp1Cookie)
                .andExpect(status().isOk());

        // After: count should be 1
        performGetWithCookie(NOTIF_URL + "/unread-count", emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // User scoping — cannot mark another user's notification as read
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: cannot mark another user's notification as read → 401")
    void employee_CannotMarkOtherUsersNotificationAsRead_Returns401() throws Exception {
        // Seed a notification owned by emp2
        Notification emp2Notif = seedNotification(emp2UserId);

        // emp1 tries to mark emp2's notification as read
        // NotificationService throws UnauthorizedException → GlobalExceptionHandler → 401
        performPatchWithCookie(NOTIF_URL + "/" + emp2Notif.getId() + "/read", emp1Cookie)
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Notification preferences
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: PUT /preferences updates notification preferences")
    void employee_UpdatesNotificationPreferences() throws Exception {
        NotificationPreferenceRequest req = NotificationPreferenceRequest.builder()
                .emailEnabled(false)
                .inAppEnabled(true)
                .taskAssigned(true)
                .taskOverdue(false)
                .dailyDigest(false)
                .build();

        performPutWithCookie(NOTIF_URL + "/preferences", req, emp1Cookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailEnabled").value(false))
                .andExpect(jsonPath("$.data.inAppEnabled").value(true));
    }

    @Test
    @DisplayName("employee: GET /preferences returns current preferences")
    void employee_GetPreferences_Returns200() throws Exception {
        performGetWithCookie(NOTIF_URL + "/preferences", emp1Cookie)
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Unauthenticated access
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("unauthenticated: GET /notifications returns 401")
    void unauthenticated_Returns401() throws Exception {
        performGet(NOTIF_URL)
                .andExpect(status().isUnauthorized());
    }
}
