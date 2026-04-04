package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.model.RefreshToken;
import com.digiwork.taskhive.module.auth.repository.RefreshTokenRepository;
import com.digiwork.taskhive.module.auth.security.JwtConfig;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Access Token Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Access Token Operations")
    class AccessTokenTests {

        @Test
        @DisplayName("should generate an access token by delegating to JwtTokenProvider")
        void shouldGenerateAccessToken() {
            when(jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN"))
                    .thenReturn("jwt-token");

            String token = tokenService.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            assertThat(token).isEqualTo("jwt-token");
            verify(jwtTokenProvider).generateAccessToken(testUserId, "user@test.com", "ADMIN");
        }

        @Test
        @DisplayName("should validate access token")
        void shouldValidateAccessToken() {
            when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);

            assertThat(tokenService.validateAccessToken("valid-token")).isTrue();
        }

        @Test
        @DisplayName("should get userId from access token")
        void shouldGetUserIdFromAccessToken() {
            when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(testUserId);

            assertThat(tokenService.getUserIdFromAccessToken("token")).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("should get email from access token")
        void shouldGetEmailFromAccessToken() {
            when(jwtTokenProvider.getEmailFromToken("token")).thenReturn("user@test.com");

            assertThat(tokenService.getEmailFromAccessToken("token")).isEqualTo("user@test.com");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Refresh Token Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Refresh Token Operations")
    class RefreshTokenTests {

        @Test
        @DisplayName("should generate and store a refresh token")
        void shouldGenerateAndStoreRefreshToken() {
            when(jwtConfig.getRefreshTokenExpiry()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String rawToken = tokenService.generateRefreshToken(testUserId);

            assertThat(rawToken).isNotNull().isNotEmpty();
            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            RefreshToken saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(testUserId);
            assertThat(saved.getTokenHash()).isNotEmpty();
            assertThat(saved.getRevoked()).isFalse();
        }

        @Test
        @DisplayName("should validate a valid refresh token")
        void shouldValidateRefreshToken_whenValid() {
            String rawToken = "test-raw-token";
            String tokenHash = tokenService.hashToken(rawToken);
            RefreshToken storedToken = RefreshToken.builder()
                    .userId(testUserId).tokenHash(tokenHash).revoked(false)
                    .expiresAt(LocalDateTime.now().plusHours(1)).build();

            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(storedToken));

            RefreshToken result = tokenService.validateRefreshToken(rawToken);

            assertThat(result).isEqualTo(storedToken);
        }

        @Test
        @DisplayName("should throw InvalidTokenException when refresh token not found")
        void shouldThrowException_whenRefreshTokenNotFound() {
            String rawToken = "nonexistent-token";
            String tokenHash = tokenService.hashToken(rawToken);

            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tokenService.validateRefreshToken(rawToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("should throw TokenExpiredException when refresh token is expired")
        void shouldThrowException_whenRefreshTokenExpired() {
            String rawToken = "expired-token";
            String tokenHash = tokenService.hashToken(rawToken);
            RefreshToken expiredToken = RefreshToken.builder()
                    .userId(testUserId).tokenHash(tokenHash).revoked(false)
                    .expiresAt(LocalDateTime.now().minusHours(1)).build();

            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> tokenService.validateRefreshToken(rawToken))
                    .isInstanceOf(TokenExpiredException.class);
        }

        @Test
        @DisplayName("should revoke a refresh token")
        void shouldRevokeRefreshToken() {
            String rawToken = "revoke-me";
            String tokenHash = tokenService.hashToken(rawToken);
            RefreshToken token = RefreshToken.builder()
                    .userId(testUserId).tokenHash(tokenHash).revoked(false).build();

            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(token));

            tokenService.revokeRefreshToken(rawToken);

            assertThat(token.getRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("should revoke all refresh tokens for a user")
        void shouldRevokeAllRefreshTokens() {
            tokenService.revokeAllRefreshTokens(testUserId);

            verify(refreshTokenRepository).revokeAllByUserId(testUserId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Hash Token Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("hashToken")
    class HashTokenTests {

        @Test
        @DisplayName("should produce consistent hash for the same input")
        void shouldHashTokenConsistently() {
            String rawToken = "consistent-test-token";

            String hash1 = tokenService.hashToken(rawToken);
            String hash2 = tokenService.hashToken(rawToken);

            assertThat(hash1)
                    .isEqualTo(hash2)
                    .isNotEmpty();
        }

        @Test
        @DisplayName("should produce different hashes for different inputs")
        void shouldProduceDifferentHashes() {
            String hash1 = tokenService.hashToken("token-a");
            String hash2 = tokenService.hashToken("token-b");

            assertThat(hash1).isNotEqualTo(hash2);
        }
    }
}
