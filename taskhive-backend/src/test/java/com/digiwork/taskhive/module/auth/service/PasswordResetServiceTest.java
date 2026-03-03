package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.dto.ForgotPasswordRequest;
import com.digiwork.taskhive.module.auth.dto.ResetPasswordRequest;
import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.model.PasswordResetToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.PasswordResetTokenRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .passwordHash("currentHash")
                .build();

        ReflectionTestUtils.setField(passwordResetService, "resetTokenExpiryMs", 3600000L);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // forgotPassword Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("forgotPassword")
    class ForgotPasswordTests {

        @Test
        @DisplayName("should generate reset token when email exists")
        void shouldGenerateResetToken_whenEmailExists() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("test@example.com");

            when(userRepository.findByEmailAndIsDeletedFalse("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(tokenService.hashToken(anyString())).thenReturn("hashedToken");

            passwordResetService.forgotPassword(request);

            verify(resetTokenRepository).save(any(PasswordResetToken.class));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should silently return when email does not exist (prevent enumeration)")
        void shouldSilentlyReturn_whenEmailDoesNotExist() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("unknown@example.com");

            when(userRepository.findByEmailAndIsDeletedFalse("unknown@example.com"))
                    .thenReturn(Optional.empty());

            assertThatCode(() -> passwordResetService.forgotPassword(request))
                    .doesNotThrowAnyException();

            verify(resetTokenRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // resetPassword Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("rawToken");
            request.setNewPassword("NewPass1!");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(testUserId).tokenHash("hash").used(false)
                    .expiresAt(LocalDateTime.now().plusHours(1)).build();

            when(tokenService.hashToken("rawToken")).thenReturn("hash");
            when(resetTokenRepository.findByTokenHashAndUsedFalse("hash"))
                    .thenReturn(Optional.of(resetToken));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordService.encode("NewPass1!")).thenReturn("newEncodedPassword");

            passwordResetService.resetPassword(request);

            verify(passwordService).validateNotReused(testUser, "NewPass1!");
            verify(passwordService).saveToHistory(testUser);
            verify(userRepository).save(testUser);
            verify(tokenService).revokeAllRefreshTokens(testUserId);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("should throw InvalidTokenException when token not found")
        void shouldThrowException_whenResetTokenInvalid() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("badToken");
            request.setNewPassword("NewPass1!");

            when(tokenService.hashToken("badToken")).thenReturn("badHash");
            when(resetTokenRepository.findByTokenHashAndUsedFalse("badHash"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("should throw TokenExpiredException when token is expired")
        void shouldThrowException_whenResetTokenExpired() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("rawToken");
            request.setNewPassword("NewPass1!");

            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .userId(testUserId).tokenHash("hash").used(false)
                    .expiresAt(LocalDateTime.now().minusHours(1)).build();

            when(tokenService.hashToken("rawToken")).thenReturn("hash");
            when(resetTokenRepository.findByTokenHashAndUsedFalse("hash"))
                    .thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(TokenExpiredException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when new password is reused")
        void shouldThrowException_whenPasswordReused() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("rawToken");
            request.setNewPassword("OldPass1!");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(testUserId).tokenHash("hash").used(false)
                    .expiresAt(LocalDateTime.now().plusHours(1)).build();

            when(tokenService.hashToken("rawToken")).thenReturn("hash");
            when(resetTokenRepository.findByTokenHashAndUsedFalse("hash"))
                    .thenReturn(Optional.of(resetToken));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            doThrow(new BusinessException("Password same as old"))
                    .when(passwordService).validateNotReused(testUser, "OldPass1!");

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
