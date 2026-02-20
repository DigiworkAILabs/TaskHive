package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.module.auth.dto.ActivateAccountRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.event.AccountActivatedEvent;
import com.digiwork.taskhive.module.auth.exception.TokenAlreadyUsedException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.model.AccountActivationToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.AccountActivationTokenRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final AccountActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        String tokenHash = tokenService.hashToken(request.getToken());

        AccountActivationToken activationToken = activationTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(MessageConstants.INVALID_TOKEN));

        if (activationToken.getUsed()) {
            throw new TokenAlreadyUsedException(MessageConstants.TOKEN_ALREADY_USED);
        }

        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(MessageConstants.TOKEN_EXPIRED);
        }

        // Mark token as used
        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);

        // Get user and set password
        User user = userRepository.findById(activationToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        user.setPasswordHash(passwordService.encode(request.getNewPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Save to password history
        passwordService.saveToHistory(user);

        // Publish event
        eventPublisher.publishEvent(new AccountActivatedEvent(this, user.getId(), user.getEmail()));

        log.info("Account activated for user: {}", user.getEmail());
    }
}
