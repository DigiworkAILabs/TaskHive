package com.digiwork.taskhive.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.TimeZone;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    // ── Admin identity — must match app.admin.email in application-test.properties ──
    protected static final String ADMIN_EMAIL = "admin@test.com";

    // ── Shared Testcontainers PostgreSQL ──────────────────────────────────────
    protected static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("taskhive_test")
                .withUsername("test")
                .withPassword("test")
                .withCommand("-c", "timezone=UTC");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    // ── Shared Beans ──────────────────────────────────────────────────────────

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JavaMailSender mailSender;

    // ── All repositories needed for FK-safe cleanup ───────────────────────────

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.UserRepository userRepository;

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.UserRoleRepository userRoleRepository;

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.AccountActivationTokenRepository accountActivationTokenRepository;

    @Autowired
    protected com.digiwork.taskhive.module.auth.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    protected com.digiwork.taskhive.module.audit.repository.AuditLogRepository auditLogRepository;

    @Autowired
    protected com.digiwork.taskhive.module.audit.repository.SecurityEventRepository securityEventRepository;

    @Autowired
    protected com.digiwork.taskhive.module.employee.repository.EmployeeRepository employeeRepository;

    @Autowired
    protected com.digiwork.taskhive.module.employee.repository.EmployeeStatusHistoryRepository employeeStatusHistoryRepository;

    @Autowired
    protected com.digiwork.taskhive.module.notification.repository.NotificationRepository notificationRepository;

    @Autowired
    protected com.digiwork.taskhive.module.notification.repository.NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    protected com.digiwork.taskhive.module.notification.repository.EmailQueueRepository emailQueueRepository;

    @Autowired
    protected com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository taskStatusHistoryRepository;

    @Autowired
    protected com.digiwork.taskhive.module.task.repository.TaskCommentRepository taskCommentRepository;

    @Autowired
    protected com.digiwork.taskhive.module.task.repository.TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    protected com.digiwork.taskhive.module.task.repository.TaskRepository taskRepository;

    // ── Definitive FK-safe cleanup ────────────────────────────────────────────
    /**
     * Deletes ALL test-created data in correct FK dependency order.
     *
     * The seeded admin user AND their ADMIN role assignment are intentionally
     * preserved. The original bug that caused every admin-only endpoint to return
     * 403 was that userRoleRepository.deleteAll() wiped the admin's ADMIN role
     * row, leaving the admin as a user with no roles. The next login then
     * produced a JWT with role=EMPLOYEE (the empty-list fallback), so Spring
     * Security never granted ROLE_ADMIN, and every @PreAuthorize("hasRole('ADMIN')")
     * check failed.
     *
     * Fix: resolve the admin's UUID once, then skip their user_roles row during
     * the role-cleanup step, exactly the same way we already skip their user row.
     *
     * Full FK graph rooted at users:
     *   security_events   → users(user_id)
     *   audit_logs        → users(actor_id)
     *   notifications     → users(user_id)
     *   notification_prefs→ users(user_id)
     *   email_queue       → (no FK to users, safe anytime)
     *   task_attachments  → tasks(task_id)
     *   task_comments     → tasks(task_id)
     *   task_status_hist  → tasks(task_id)
     *   tasks             → employees(assigned_to) + users(created_by)
     *   emp_status_hist   → employees(employee_id)
     *   employees         → users(user_id)
     *   password_history  → users(user_id)
     *   account_act_tokens→ users(user_id)
     *   password_reset_tok→ users(user_id)
     *   refresh_tokens    → users(user_id)
     *   user_roles        → users(user_id)
     */
    protected void cleanupAllTestData() {
        // Resolve admin UUID up front so we can exclude their rows below.
        // findByEmailAndIsDeletedFalse is safe here — admin is never soft-deleted.
        java.util.Optional<com.digiwork.taskhive.module.auth.model.User> adminOpt =
                userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL);

        // Tier 1 — direct children of users with no children themselves
        securityEventRepository.deleteAll();
        auditLogRepository.deleteAll();
        notificationRepository.deleteAll();
        notificationPreferenceRepository.deleteAll();
        emailQueueRepository.deleteAll();

        // Tier 2 — task children (must go before tasks)
        taskAttachmentRepository.deleteAll();
        taskCommentRepository.deleteAll();
        taskStatusHistoryRepository.deleteAll();

        // Tier 3 — tasks
        taskRepository.deleteAll();

        // Tier 4 — employee children (must go before employees)
        employeeStatusHistoryRepository.deleteAll();

        // Tier 5 — employees (must go before users)
        employeeRepository.deleteAll();

        // Tier 6 — remaining user children
        passwordHistoryRepository.deleteAll();
        accountActivationTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();

        // ── user_roles: preserve the admin's ADMIN role assignment ────────────
        // deleteAll() would destroy the admin's role row, making every subsequent
        // test login produce a JWT with role=EMPLOYEE and causing 403 on all
        // @PreAuthorize("hasRole('ADMIN')") endpoints.
        if (adminOpt.isPresent()) {
            java.util.UUID adminUserId = adminOpt.get().getId();
            userRoleRepository.findAll().stream()
                    .filter(ur -> !ur.getUserId().equals(adminUserId))
                    .forEach(userRoleRepository::delete);
        } else {
            // Admin doesn't exist yet (e.g., first run before seeding) — safe to wipe all.
            userRoleRepository.deleteAll();
        }

        // Tier 7 — users last; preserve the seeded admin
        userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals(ADMIN_EMAIL))
                .forEach(userRepository::delete);
    }

    // ── HTTP Helpers — No Cookies ─────────────────────────────────────────────

    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performPatch(String url) throws Exception {
        return mockMvc.perform(patch(url));
    }

    protected ResultActions performPatchWithBody(String url, Object body) throws Exception {
        return mockMvc.perform(
                patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url));
    }

    // ── HTTP Helpers — With Cookies ───────────────────────────────────────────

    protected ResultActions performGetWithCookie(String url, Cookie... cookies) throws Exception {
        return mockMvc.perform(get(url).cookie(cookies));
    }

    protected ResultActions performPostWithCookie(String url, Object body, Cookie... cookies) throws Exception {
        return mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .cookie(cookies));
    }

    protected ResultActions performPutWithCookie(String url, Object body, Cookie... cookies) throws Exception {
        return mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .cookie(cookies));
    }

    protected ResultActions performPatchWithCookie(String url, Cookie... cookies) throws Exception {
        return mockMvc.perform(patch(url).cookie(cookies));
    }

    protected ResultActions performPatchWithBodyAndCookie(String url, Object body, Cookie... cookies) throws Exception {
        return mockMvc.perform(
                patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .cookie(cookies));
    }

    protected ResultActions performDeleteWithCookie(String url, Cookie... cookies) throws Exception {
        return mockMvc.perform(delete(url).cookie(cookies));
    }
}
