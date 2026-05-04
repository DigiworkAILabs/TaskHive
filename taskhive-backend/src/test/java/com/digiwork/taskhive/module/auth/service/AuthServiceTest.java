package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.util.CookieUtil;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.exception.AccountLockedException;
import com.digiwork.taskhive.module.auth.exception.AccountNotActiveException;
import com.digiwork.taskhive.module.auth.exception.InvalidCredentialsException;
import com.digiwork.taskhive.module.auth.model.RefreshToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private CookieUtil cookieUtil;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AuditService auditService;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("encodedPassword")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .isDeleted(false)
                .build();

        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMs", 900000L);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext() {
        CustomUserDetails userDetails = new CustomUserDetails(
                testUserId, "test@example.com", "encodedPassword", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // login Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void shouldLoginSuccessfully_whenCredentialsValid() {
            // given
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(testUserId.toString()).email("test@example.com")
                    .firstName("Test").lastName("User").role("EMPLOYEE").build();

            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordService.matches("password123", "encodedPassword")).thenReturn(true);
            when(userRoleRepository.findRoleNamesByUserId(testUserId)).thenReturn(List.of("EMPLOYEE"));
            when(tokenService.generateAccessToken(testUserId, "test@example.com", "EMPLOYEE", 0L))
                    .thenReturn("accessToken");
            when(tokenService.generateRefreshToken(testUserId)).thenReturn("refreshToken");
            when(userMapper.toUserInfoResponse(testUser, List.of("EMPLOYEE"))).thenReturn(userInfo);

            // when
            LoginResponse result = authService.login(request, response);

            // then
            assertThat(result.getUser()).isEqualTo(userInfo);
            verify(cookieUtil).addAccessTokenCookie(response, "accessToken");
            verify(cookieUtil).addRefreshTokenCookie(response, "refreshToken");
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when user not found")
        void shouldThrowException_whenUserNotFound() {
            LoginRequest request = new LoginRequest("unknown@example.com", "password");
            when(userRepository.findByEmailAndIsDeletedFalse("unknown@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should throw AccountLockedException when account is locked")
        void shouldThrowException_whenAccountLocked() {
            testUser.setLockedUntil(LocalDateTime.now().plusHours(1));
            LoginRequest request = new LoginRequest("test@example.com", "password");
            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("should throw AccountNotActiveException when status is not ACTIVE")
        void shouldThrowException_whenAccountNotActive() {
            testUser.setStatus(UserStatus.PENDING);
            LoginRequest request = new LoginRequest("test@example.com", "password");
            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(AccountNotActiveException.class);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when password is incorrect")
        void shouldThrowException_whenPasswordIncorrect() {
            LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordService.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(InvalidCredentialsException.class);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should increment failedAttempts when password is incorrect")
        void shouldIncrementFailedAttempts_whenPasswordIncorrect() {
            LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordService.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(InvalidCredentialsException.class);

            assertThat(testUser.getFailedAttempts()).isEqualTo(1);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should lock account after maxFailedAttempts consecutive failures")
        void shouldLockAccount_whenMaxAttemptsExceeded() {
            testUser.setFailedAttempts(4); // 1 more will trigger lockout (maxFailedAttempts = 5)
            LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordService.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request, response))
                    .isInstanceOf(InvalidCredentialsException.class);

            assertThat(testUser.getLockedUntil()).isNotNull();
            assertThat(testUser.getLockedUntil()).isAfter(LocalDateTime.now());
            verify(userRepository).save(testUser);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // logout Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("should logout successfully and clear cookies")
        void shouldLogoutSuccessfully() {
            setSecurityContext();

            authService.logout("refreshTokenValue", response);

            verify(tokenService).revokeRefreshToken("refreshTokenValue");
            verify(cookieUtil).clearAllAuthCookies(response);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should logout without revoking token when token is null")
        void shouldLogoutWithoutRevokingToken_whenTokenNull() {
            setSecurityContext();

            authService.logout(null, response);

            verify(tokenService, never()).revokeRefreshToken(anyString());
            verify(cookieUtil).clearAllAuthCookies(response);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // refresh Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        @DisplayName("should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            RefreshToken refreshToken = RefreshToken.builder()
                    .userId(testUserId).tokenHash("hash").revoked(false)
                    .expiresAt(LocalDateTime.now().plusHours(1)).build();

            when(tokenService.validateRefreshToken("rawToken")).thenReturn(refreshToken);
            when(userRepository.findByIdAndIsDeletedFalse(testUserId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findRoleNamesByUserId(testUserId)).thenReturn(List.of("EMPLOYEE"));
            when(tokenService.generateAccessToken(testUserId, "test@example.com", "EMPLOYEE", 0L))
                    .thenReturn("newAccessToken");

            authService.refresh("rawToken", response);

            verify(cookieUtil).addAccessTokenCookie(response, "newAccessToken");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getCurrentUser Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return current user info")
        void shouldGetCurrentUser() {
            setSecurityContext();
            UserInfoResponse expectedResponse = UserInfoResponse.builder()
                    .id(testUserId.toString()).email("test@example.com").build();

            when(userRepository.findByIdAndIsDeletedFalse(testUserId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findRoleNamesByUserId(testUserId)).thenReturn(List.of("EMPLOYEE"));
            when(userMapper.toUserInfoResponse(testUser, List.of("EMPLOYEE"))).thenReturn(expectedResponse);

            UserInfoResponse result = authService.getCurrentUser();

            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // changePassword Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() {
            setSecurityContext();
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "NewPass1!");

            when(userRepository.findByIdAndIsDeletedFalse(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordService.matches("oldPass", "encodedPassword")).thenReturn(true);
            when(passwordService.encode("NewPass1!")).thenReturn("newEncodedPassword");

            authService.changePassword(request, response);

            verify(passwordService).validateNotReused(testUser, "NewPass1!");
            verify(passwordService).saveToHistory(testUser);
            verify(userRepository).save(testUser);
            verify(tokenService).revokeAllRefreshTokens(testUserId);
            verify(cookieUtil).clearAllAuthCookies(response);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should throw exception when old password is incorrect")
        void shouldThrowException_whenOldPasswordIncorrect() {
            setSecurityContext();
            ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPass", "NewPass1!");

            when(userRepository.findByIdAndIsDeletedFalse(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordService.matches("wrongOldPass", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(request, response))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
