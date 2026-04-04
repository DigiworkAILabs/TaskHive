package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.*;
import com.digiwork.taskhive.module.auth.repository.*;
import com.digiwork.taskhive.module.auth.service.PasswordService;
import com.digiwork.taskhive.module.auth.service.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthFlowIntegrationTest
 * ────────────────────────
 * Full HTTP integration tests for all /api/v1/auth endpoints.
 *
 * Key design decisions:
 * - No @Transactional on class: login() creates refresh_tokens in a committed
 *   sub-transaction; a test-level rollback would never see it, and the next
 *   setUp() would fail with a duplicate-email constraint.
 * - @AfterEach calls cleanupAllTestData() which deletes in FK-safe order and
 *   restores the admin account to a clean state so every test starts fresh.
 * - The admin user (admin@test.com) is seeded by AdminSeeder on startup and
 *   is never deleted — only its mutable state (failedAttempts, password) is
 *   reset in @AfterEach.
 */
class AuthFlowIntegrationTest extends BaseIntegrationTest {

    private static final String LOGIN_URL       = "/api/v1/auth/login";
    private static final String LOGOUT_URL      = "/api/v1/auth/logout";
    private static final String REFRESH_URL     = "/api/v1/auth/refresh";
    private static final String ACTIVATE_URL    = "/api/v1/auth/activate-account";
    private static final String FORGOT_URL      = "/api/v1/auth/forgot-password";
    private static final String RESET_URL       = "/api/v1/auth/reset-password";
    private static final String CHANGE_PASS_URL = "/api/v1/auth/change-password";
    private static final String ME_URL          = "/api/v1/auth/me";

    private static final String ADMIN_EMAIL        = "admin@test.com";
    private static final String ADMIN_PASSWORD     = "Admin@123";
    private static final String VALID_NEW_PASSWORD = "NewPass@123";

    @Autowired private RoleRepository     roleRepository;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private TokenService       tokenService;
    @Autowired private PasswordService    passwordService;

    @Value("${app.auth.max-failed-attempts}")
    private int maxFailedAttempts;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MvcResult loginAs(String email, String password) throws Exception {
        return performPost(LOGIN_URL, new LoginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Cookie extractCookie(MvcResult result, String cookieName) {
        Cookie cookie = result.getResponse().getCookie(cookieName);
        assertThat(cookie).as("Cookie '%s' should be present", cookieName).isNotNull();
        return cookie;
    }

    /**
     * Creates a PENDING user with a known activation token. Returns the raw token.
     */
    private String createPendingUserWithToken(String email) {
        var role = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role not found"));

        User user = User.builder()
                .email(email)
                .passwordHash("placeholder")
                .firstName("Test")
                .lastName("Employee")
                .status(UserStatus.PENDING)
                .failedAttempts(0)
                .isDeleted(false)
                .build();
        user = userRepository.save(user);

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build());

        String rawToken  = "test-activation-token-" + email;
        String tokenHash = tokenService.hashToken(rawToken);

        accountActivationTokenRepository.save(AccountActivationToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build());

        return rawToken;
    }

    @AfterEach
    void cleanup() {
        // 1. Full FK-safe wipe of all test data
        cleanupAllTestData();

        // 2. Restore admin account to pristine state.
        //    login() commits refresh_tokens and security_events in REQUIRES_NEW
        //    sub-transactions — cleanupAllTestData() deletes those, but we also
        //    need to reset mutable fields that tests may have dirtied.
        userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).ifPresent(u -> {
            u.setFailedAttempts(0);
            u.setLockedUntil(null);
            u.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            userRepository.save(u);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Login
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("login: should return 200 and set accessToken, refreshToken, userRole cookies")
    void login_Success() throws Exception {
        MvcResult result = performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value(ADMIN_EMAIL))
                // UserMapper.toUserInfoResponse sets role from the roles list returned by
                // userRoleRepository.findRoleNamesByUserId — it returns "ADMIN" (without prefix).
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                .andReturn();

        assertThat(result.getResponse().getCookie("accessToken")).isNotNull();
        assertThat(result.getResponse().getCookie("refreshToken")).isNotNull();
        assertThat(result.getResponse().getCookie("userRole")).isNotNull();
    }

    @Test
    @DisplayName("login: should return 401 when user does not exist")
    void login_UserNotFound() throws Exception {
        performPost(LOGIN_URL, new LoginRequest("nobody@example.com", "SomePass@1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login: should return 401 when password is incorrect")
    void login_WrongPassword() throws Exception {
        performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, "WrongPass@1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login: should return 403 when account status is PENDING")
    void login_AccountNotActive() throws Exception {
        User pendingUser = User.builder()
                .email("pending@test.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .firstName("Pending").lastName("User")
                .status(UserStatus.PENDING)
                .failedAttempts(0).isDeleted(false)
                .build();
        userRepository.save(pendingUser);

        performPost(LOGIN_URL, new LoginRequest("pending@test.com", "Admin@123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("login: should return 423 when account is locked")
    void login_AccountLocked() throws Exception {
        User lockedUser = User.builder()
                .email("locked@test.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .firstName("Locked").lastName("User")
                .status(UserStatus.ACTIVE)
                .failedAttempts(maxFailedAttempts)
                .lockedUntil(LocalDateTime.now().plusHours(1))
                .isDeleted(false)
                .build();
        userRepository.save(lockedUser);

        performPost(LOGIN_URL, new LoginRequest("locked@test.com", "Admin@123"))
                .andExpect(status().isLocked());
    }

    @Test
    @DisplayName("login: should increment failedAttempts in DB on wrong password")
    void login_IncrementsFailedAttempts() throws Exception {
        // Use a fresh dedicated user so we never touch the shared admin account.
        // Touching admin risks leaving failedAttempts > 0, which would cause
        // subsequent tests that login as admin to get 423 even after @AfterEach reset
        // (because the reset runs AFTER the next @BeforeEach of another test class
        // in the same JVM run).
        User freshUser = User.builder()
                .email("failcount@test.com")
                .passwordHash(passwordEncoder.encode("Correct@123"))
                .firstName("Fail").lastName("Count")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0).isDeleted(false)
                .build();
        userRepository.save(freshUser);

        // One wrong-password attempt
        performPost(LOGIN_URL, new LoginRequest("failcount@test.com", "Wrong@123"))
                .andExpect(status().isUnauthorized());

        User after = userRepository.findByEmailAndIsDeletedFalse("failcount@test.com").orElseThrow();
        assertThat(after.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("login: should return 400 when email format is invalid")
    void login_InvalidEmailFormat() throws Exception {
        performPost(LOGIN_URL, new LoginRequest("not-an-email", "Admin@123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("login: should lock account and return 423 after max failed attempts")
    void login_AutoLocksAccountAfterMaxFailedAttempts() throws Exception {
        User user = User.builder()
                .email("autolock@test.com")
                .passwordHash(passwordEncoder.encode("Correct@123"))
                .firstName("Auto").lastName("Lock")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0).isDeleted(false)
                .build();
        userRepository.save(user);

        for (int i = 0; i < maxFailedAttempts; i++) {
            performPost(LOGIN_URL, new LoginRequest("autolock@test.com", "Wrong@123"))
                    .andExpect(status().isUnauthorized());
        }

        performPost(LOGIN_URL, new LoginRequest("autolock@test.com", "Correct@123"))
                .andExpect(status().isLocked());

        User locked = userRepository.findByEmailAndIsDeletedFalse("autolock@test.com").orElseThrow();
        assertThat(locked.getFailedAttempts()).isGreaterThanOrEqualTo(maxFailedAttempts);
        assertThat(locked.isAccountLocked()).isTrue();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Logout
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("logout: should return 200 and clear all auth cookies")
    void logout_Success() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken  = extractCookie(loginResult, "accessToken");
        Cookie refreshToken = extractCookie(loginResult, "refreshToken");

        MvcResult logoutResult = performPostWithCookie(LOGOUT_URL, null, accessToken, refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        Cookie clearedAccess  = logoutResult.getResponse().getCookie("accessToken");
        Cookie clearedRefresh = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(clearedAccess).isNotNull();
        assertThat(clearedAccess.getMaxAge()).isZero();
        assertThat(clearedRefresh).isNotNull();
        assertThat(clearedRefresh.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("logout: should return 200 even without a refresh token cookie")
    void logout_WithoutRefreshToken() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken = extractCookie(loginResult, "accessToken");

        performPostWithCookie(LOGOUT_URL, null, accessToken)
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Refresh Token
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("refresh: should return 200 and issue a new accessToken cookie")
    void refresh_Success() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie refreshToken = extractCookie(loginResult, "refreshToken");

        MvcResult refreshResult = performPostWithCookie(REFRESH_URL, null, refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        assertThat(refreshResult.getResponse().getCookie("accessToken")).isNotNull();
    }

    @Test
    @DisplayName("refresh: should return 400 when no refresh token cookie is present")
    void refresh_NoToken() throws Exception {
        performPost(REFRESH_URL, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("refresh: should return 400 when refresh token is invalid")
    void refresh_InvalidToken() throws Exception {
        Cookie fakeToken = new Cookie("refreshToken", "invalid-token-value");

        performPostWithCookie(REFRESH_URL, null, fakeToken)
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Get Current User
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("me: should return 200 with current user info when authenticated")
    void getCurrentUser_Success() throws Exception {
        // Login first to get a valid committed accessToken
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken = extractCookie(loginResult, "accessToken");

        performGetWithCookie(ME_URL, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL))
                // UserMapper returns the raw role name from DB: "ADMIN"
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("me: should return 401 when no access token is present")
    void getCurrentUser_Unauthenticated() throws Exception {
        performGet(ME_URL)
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Account Activation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("activate-account: should return 200 and set user status to ACTIVE")
    void activateAccount_Success() throws Exception {
        String rawToken = createPendingUserWithToken("newemployee@test.com");

        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User activated = userRepository.findByEmailAndIsDeletedFalse("newemployee@test.com").orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("activate-account: should return 400 when token is invalid")
    void activateAccount_InvalidToken() throws Exception {
        performPost(ACTIVATE_URL, new ActivateAccountRequest("invalid-token", VALID_NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("activate-account: should return 400 when token is expired")
    void activateAccount_ExpiredToken() throws Exception {
        String rawToken = createPendingUserWithToken("expired@test.com");

        String tokenHash = tokenService.hashToken(rawToken);
        AccountActivationToken token = accountActivationTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash).orElseThrow();
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        accountActivationTokenRepository.save(token);

        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("activate-account: should return 400 when token has already been used")
    void activateAccount_TokenAlreadyUsed() throws Exception {
        String rawToken = createPendingUserWithToken("usedtoken@test.com");

        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isOk());

        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("activate-account: should return 400 when password does not meet complexity rules")
    void activateAccount_WeakPassword() throws Exception {
        String rawToken = createPendingUserWithToken("weakpass@test.com");

        performPost(ACTIVATE_URL, new ActivateAccountRequest(rawToken, "weak"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Forgot Password
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("forgot-password: should return 200 for a known email and save reset token")
    void forgotPassword_KnownEmail() throws Exception {
        performPost(FORGOT_URL, new ForgotPasswordRequest(ADMIN_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User admin = userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).orElseThrow();
        assertThat(passwordResetTokenRepository.findAll())
                .anyMatch(t -> t.getUserId().equals(admin.getId()) && !t.isUsed());
    }

    @Test
    @DisplayName("forgot-password: should return 200 for an unknown email (anti-enumeration)")
    void forgotPassword_UnknownEmail() throws Exception {
        performPost(FORGOT_URL, new ForgotPasswordRequest("doesnotexist@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Reset Password
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("reset-password: should return 200 and allow login with new password")
    void resetPassword_Success() throws Exception {
        String rawToken  = "test-reset-token-admin";
        String tokenHash = tokenService.hashToken(rawToken);
        User admin = userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).orElseThrow();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(admin.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build());

        performPost(RESET_URL, new ResetPasswordRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, VALID_NEW_PASSWORD))
                .andExpect(status().isOk());

        performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("reset-password: should return 400 for an invalid token")
    void resetPassword_InvalidToken() throws Exception {
        performPost(RESET_URL, new ResetPasswordRequest("invalid-token", VALID_NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset-password: should return 400 for an expired token")
    void resetPassword_ExpiredToken() throws Exception {
        String rawToken = "test-reset-token-expired";
        User admin = userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).orElseThrow();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(admin.getId())
                .tokenHash(tokenService.hashToken(rawToken))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build());

        performPost(RESET_URL, new ResetPasswordRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset-password: should return 400 when token has already been used")
    void resetPassword_TokenAlreadyUsed() throws Exception {
        String rawToken = "test-reset-token-reuse";
        User admin = userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).orElseThrow();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(admin.getId())
                .tokenHash(tokenService.hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build());

        performPost(RESET_URL, new ResetPasswordRequest(rawToken, VALID_NEW_PASSWORD))
                .andExpect(status().isOk());

        performPost(RESET_URL, new ResetPasswordRequest(rawToken, "AnotherPass@456"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset-password: should return 400 when new password matches a recent password in history")
    void resetPassword_RejectsReusedPassword() throws Exception {
        // Use a dedicated fresh user so this test never mutates admin password history,
        // which would cause resetPassword_Success to fail if it runs afterward.
        User freshUser = User.builder()
                .email("historycheck@test.com")
                .passwordHash(passwordEncoder.encode("CurrentPass@1"))
                .firstName("History").lastName("Check")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0).isDeleted(false)
                .build();
        freshUser = userRepository.save(freshUser);

        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        userRoleRepository.save(UserRole.builder()
                .userId(freshUser.getId()).roleId(role.getId()).build());

        // Save current password into history (simulates a prior password change)
        passwordService.saveToHistory(freshUser);

        String rawToken = "test-reset-reuse-history";
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(freshUser.getId())
                .tokenHash(tokenService.hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build());

        // Attempt to reset to "CurrentPass@1" — which is in history — must fail with 400
        performPost(RESET_URL, new ResetPasswordRequest(rawToken, "CurrentPass@1"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Change Password
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("change-password: should return 200 and revoke all refresh tokens")
    void changePassword_Success() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken = extractCookie(loginResult, "accessToken");

        performPostWithCookie(CHANGE_PASS_URL, new ChangePasswordRequest(ADMIN_PASSWORD, VALID_NEW_PASSWORD), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User admin = userRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).orElseThrow();
        boolean anyActive = refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(admin.getId()))
                .anyMatch(t -> !t.getRevoked());
        assertThat(anyActive).isFalse();
    }

    @Test
    @DisplayName("change-password: should return 401 when old password is wrong")
    void changePassword_WrongOldPassword() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken = extractCookie(loginResult, "accessToken");

        performPostWithCookie(CHANGE_PASS_URL, new ChangePasswordRequest("WrongOld@123", VALID_NEW_PASSWORD), accessToken)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("change-password: should return 401 when not authenticated")
    void changePassword_Unauthenticated() throws Exception {
        performPost(CHANGE_PASS_URL, new ChangePasswordRequest(ADMIN_PASSWORD, VALID_NEW_PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("change-password: should return 400 when new password matches a recent password in history")
    void changePassword_RejectsReusedPassword() throws Exception {
        MvcResult loginResult = loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        Cookie accessToken = extractCookie(loginResult, "accessToken");

        // First change: Admin@123 → NewPass@123 (service saves Admin@123 to history)
        performPostWithCookie(CHANGE_PASS_URL,
                new ChangePasswordRequest(ADMIN_PASSWORD, VALID_NEW_PASSWORD), accessToken)
                .andExpect(status().isOk());

        // Second change: try to revert to Admin@123 (in history) → must fail
        performPostWithCookie(CHANGE_PASS_URL,
                new ChangePasswordRequest(VALID_NEW_PASSWORD, ADMIN_PASSWORD), accessToken)
                .andExpect(status().isBadRequest());
    }
}
