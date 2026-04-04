package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.dto.ActivateAccountRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.model.AccountActivationToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.AccountActivationTokenRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountActivationServiceTest {

    @Mock
    private AccountActivationTokenRepository activationTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountActivationService accountActivationService;

    private UUID testUserId;
    private User testUser;
    private AccountActivationToken validToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .status(UserStatus.PENDING)
                .build();

        validToken = AccountActivationToken.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .tokenHash("hashedToken")
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    @DisplayName("should activate account successfully")
    void shouldActivateAccountSuccessfully() {
        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("rawToken");
        request.setNewPassword("NewPass1!");

        when(tokenService.hashToken("rawToken")).thenReturn("hashedToken");
        when(activationTokenRepository.findByTokenHashAndUsedFalse("hashedToken"))
                .thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordService.encode("NewPass1!")).thenReturn("encodedPassword");

        accountActivationService.activateAccount(request);

        assertThat(validToken.getUsed()).isTrue();
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(testUser.getPasswordHash()).isEqualTo("encodedPassword");
        verify(activationTokenRepository).save(validToken);
        verify(userRepository).save(testUser);
        verify(passwordService).saveToHistory(testUser);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("should throw InvalidTokenException when token not found")
    void shouldThrowException_whenTokenInvalid() {
        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("badToken");
        request.setNewPassword("NewPass1!");

        when(tokenService.hashToken("badToken")).thenReturn("badHash");
        when(activationTokenRepository.findByTokenHashAndUsedFalse("badHash"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountActivationService.activateAccount(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("should throw TokenExpiredException when token is expired")
    void shouldThrowException_whenTokenExpired() {
        validToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("rawToken");
        request.setNewPassword("NewPass1!");

        when(tokenService.hashToken("rawToken")).thenReturn("hashedToken");
        when(activationTokenRepository.findByTokenHashAndUsedFalse("hashedToken"))
                .thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> accountActivationService.activateAccount(request))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user not found")
    void shouldThrowException_whenUserNotFound() {
        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("rawToken");
        request.setNewPassword("NewPass1!");

        when(tokenService.hashToken("rawToken")).thenReturn("hashedToken");
        when(activationTokenRepository.findByTokenHashAndUsedFalse("hashedToken"))
                .thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountActivationService.activateAccount(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
