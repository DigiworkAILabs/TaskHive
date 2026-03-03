package com.digiwork.taskhive.module.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // Use a test secret key (at least 32 chars for HS256)
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("ThisIsATestSecretKeyForJWTTokenSigningThatIsLongEnough");
        jwtConfig.setAccessTokenExpiry(3600000L); // 1 hour
        jwtConfig.setRefreshTokenExpiry(86400000L); // 1 day

        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        testUserId = UUID.randomUUID();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Token Generation Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate a valid non-empty JWT token")
        void shouldGenerateValidAccessToken() {
            String token = jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            assertThat(token).isNotNull().isNotEmpty();
            // JWT tokens have 3 parts separated by dots
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Token Parsing Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Token Parsing")
    class TokenParsingTests {

        @Test
        @DisplayName("should extract userId from token")
        void shouldExtractUserIdFromToken() {
            String token = jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            UUID extracted = jwtTokenProvider.getUserIdFromToken(token);

            assertThat(extracted).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("should extract email from token")
        void shouldExtractEmailFromToken() {
            String token = jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            String email = jwtTokenProvider.getEmailFromToken(token);

            assertThat(email).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should extract role from token")
        void shouldExtractRoleFromToken() {
            String token = jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            String role = jwtTokenProvider.getRoleFromToken(token);

            assertThat(role).isEqualTo("ADMIN");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Token Validation Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("should return true for a valid token")
        void shouldReturnTrue_whenTokenIsValid() {
            String token = jwtTokenProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for an invalid token")
        void shouldReturnFalse_whenTokenIsInvalid() {
            assertThat(jwtTokenProvider.validateToken("invalid.token.value")).isFalse();
        }

        @Test
        @DisplayName("should return false for a null token")
        void shouldReturnFalse_whenTokenIsNull() {
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for an expired token")
        void shouldReturnFalse_whenTokenIsExpired() {
            // Create a provider with 0ms expiry to generate an immediately expired token
            JwtConfig shortConfig = new JwtConfig();
            shortConfig.setSecret("ThisIsATestSecretKeyForJWTTokenSigningThatIsLongEnough");
            shortConfig.setAccessTokenExpiry(0L); // expires immediately
            shortConfig.setRefreshTokenExpiry(0L);

            JwtTokenProvider shortProvider = new JwtTokenProvider(shortConfig);
            String token = shortProvider.generateAccessToken(testUserId, "user@test.com", "ADMIN");

            // Small sleep to ensure token expires
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }

            assertThat(shortProvider.validateToken(token)).isFalse();
        }
    }
}
