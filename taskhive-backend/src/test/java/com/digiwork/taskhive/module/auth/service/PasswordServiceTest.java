package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.module.auth.model.PasswordHistory;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.PasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @InjectMocks
    private PasswordService passwordService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("currentEncodedPassword")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // encode / matches Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("encode and matches")
    class EncodeMatchTests {

        @Test
        @DisplayName("should encode a raw password")
        void shouldEncodePassword() {
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

            String result = passwordService.encode("rawPassword");

            assertThat(result).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("should return true when password matches")
        void shouldReturnTrueWhenPasswordMatches() {
            when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

            assertThat(passwordService.matches("raw", "encoded")).isTrue();
        }

        @Test
        @DisplayName("should return false when password does not match")
        void shouldReturnFalseWhenPasswordDoesNotMatch() {
            when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);

            assertThat(passwordService.matches("raw", "encoded")).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateNotReused Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateNotReused")
    class ValidateNotReusedTests {

        @Test
        @DisplayName("should pass validation when password is new")
        void shouldPassValidation_whenPasswordIsNew() {
            PasswordHistory history = PasswordHistory.builder()
                    .passwordHash("oldHash1").build();

            when(passwordHistoryRepository.findTop3ByUserIdOrderByCreatedAtDesc(testUser.getId()))
                    .thenReturn(List.of(history));
            when(passwordEncoder.matches("newPassword", "oldHash1")).thenReturn(false);

            passwordService.validateNotReused(testUser, "newPassword");

            // No exception thrown — test passes
        }

        @Test
        @DisplayName("should throw BusinessException when password is reused")
        void shouldThrowException_whenPasswordReused() {
            PasswordHistory history = PasswordHistory.builder()
                    .passwordHash("oldHash1").build();

            when(passwordHistoryRepository.findTop3ByUserIdOrderByCreatedAtDesc(testUser.getId()))
                    .thenReturn(List.of(history));
            when(passwordEncoder.matches("reusedPassword", "oldHash1")).thenReturn(true);

            assertThatThrownBy(() -> passwordService.validateNotReused(testUser, "reusedPassword"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // saveToHistory Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveToHistory")
    class SaveToHistoryTests {

        @Test
        @DisplayName("should save current password to history")
        void shouldSavePasswordToHistory() {
            passwordService.saveToHistory(testUser);

            ArgumentCaptor<PasswordHistory> captor = ArgumentCaptor.forClass(PasswordHistory.class);
            verify(passwordHistoryRepository).save(captor.capture());
            PasswordHistory saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(testUser.getId());
            assertThat(saved.getPasswordHash()).isEqualTo("currentEncodedPassword");
        }

        @Test
        @DisplayName("should not save to history when passwordHash is null")
        void shouldNotSaveToHistory_whenPasswordHashIsNull() {
            testUser.setPasswordHash(null);

            passwordService.saveToHistory(testUser);

            verify(passwordHistoryRepository, never()).save(any());
        }
    }
}
