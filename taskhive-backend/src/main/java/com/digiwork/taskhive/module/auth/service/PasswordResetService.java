package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.module.auth.dto.ForgotPasswordRequest;
import com.digiwork.taskhive.module.auth.dto.ResetPasswordRequest;
import com.digiwork.taskhive.module.auth.event.PasswordChangedEvent;
import com.digiwork.taskhive.module.auth.event.PasswordResetRequestedEvent;
import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.exception.TokenAlreadyUsedException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.model.PasswordResetToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.PasswordResetTokenRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.common.exception.ResourceNotFoundException;
import com.digiwork.taskhive.common.exception.RateLimitExceededException;
import com.digiwork.taskhive.common.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimitingService rateLimitingService;

    @Value("${app.auth.reset-token-expiry}")
    private long resetTokenExpiryMs;

    /**
     * Generates a password reset token and publishes an event.
     * Always returns the same message to prevent email enumeration.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String clientIp = getClientIp();
        
        // Target limit based on IP
        Bucket ipBucket = rateLimitingService.resolveBucket("forgot-pwd-ip:" + clientIp);
        if (!ipBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException("Too many password reset requests. Please try again later.");
        }

        // Target limit based on Target Email
        Bucket emailBucket = rateLimitingService.resolveBucket("forgot-pwd-email:" + request.getEmail());
        if (!emailBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for Email: {}", request.getEmail());
            throw new RateLimitExceededException("Too many password reset requests. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmailAndIsDeletedFalse(request.getEmail());

        if (userOpt.isEmpty()) {
            // Do NOT reveal whether the email exists — silently return
            log.debug("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        User user = userOpt.get();
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = tokenService.hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(resetTokenExpiryMs * 1_000_000L))
                .used(false)
                .build();

        resetTokenRepository.save(resetToken);

        // Publish event (email listener will send the email)
        eventPublisher.publishEvent(
                new PasswordResetRequestedEvent(this, user.getId(), user.getEmail(), rawToken));

        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = tokenService.hashToken(request.getToken());

        PasswordResetToken resetToken = resetTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(MessageConstants.INVALID_TOKEN));

        if (resetToken.isUsed()) {
            throw new TokenAlreadyUsedException(MessageConstants.TOKEN_ALREADY_USED);
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(MessageConstants.TOKEN_EXPIRED);
        }

        // Get user
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        // Check password history
        passwordService.validateNotReused(user, request.getNewPassword());

        // Save old password to history
        passwordService.saveToHistory(user);

        // Update password
        user.setPasswordHash(passwordService.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        // Revoke all refresh tokens
        tokenService.revokeAllRefreshTokens(user.getId());

        // Publish event
        eventPublisher.publishEvent(new PasswordChangedEvent(this, user.getId(), user.getEmail()));

        log.info("Password reset for user: {}", user.getEmail());
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xrf = request.getHeader("X-Forwarded-For");
            if (xrf != null && !xrf.isEmpty()) {
                return xrf.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }
}
