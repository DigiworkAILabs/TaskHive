package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.event.PasswordChangedEvent;
import com.digiwork.taskhive.module.auth.event.UserAuthenticatedEvent;
import com.digiwork.taskhive.module.auth.event.UserLoggedOutEvent;
import com.digiwork.taskhive.module.auth.exception.AccountLockedException;
import com.digiwork.taskhive.module.auth.exception.AccountNotActiveException;
import com.digiwork.taskhive.module.auth.exception.InvalidCredentialsException;
import com.digiwork.taskhive.module.auth.model.RefreshToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.common.exception.ResourceNotFoundException;
import com.digiwork.taskhive.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final CookieUtil cookieUtil;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.auth.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.auth.lockout-duration}")
    private long lockoutDurationMs;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(MessageConstants.INVALID_CREDENTIALS));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new AccountLockedException(MessageConstants.ACCOUNT_LOCKED);
        }

        // Check account status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException(MessageConstants.ACCOUNT_NOT_ACTIVE);
        }

        // Validate password
        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException(MessageConstants.INVALID_CREDENTIALS);
        }

        // Reset failed attempts on successful login
        resetFailedAttempts(user);

        // Get user roles
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        String primaryRole = roles.isEmpty() ? "EMPLOYEE" : roles.get(0);

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), primaryRole);
        String refreshToken = tokenService.generateRefreshToken(user.getId());

        // Set cookies
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
        cookieUtil.addUserRoleCookie(response, primaryRole);

        // Publish event
        eventPublisher.publishEvent(new UserAuthenticatedEvent(this, user.getId(), user.getEmail()));

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.builder()
                .user(userMapper.toUserInfoResponse(user, roles))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue, HttpServletResponse response) {
        UUID userId = SecurityUtils.getCurrentUserId();

        // Revoke refresh token if provided
        if (refreshTokenValue != null) {
            tokenService.revokeRefreshToken(refreshTokenValue);
        }

        // Clear cookies
        cookieUtil.clearAllAuthCookies(response);

        // Publish event
        eventPublisher.publishEvent(new UserLoggedOutEvent(this, userId));

        log.info("User logged out: {}", SecurityUtils.getCurrentUserEmail());
    }

    @Transactional
    public void refresh(String refreshTokenValue, HttpServletResponse response) {
        // Validate refresh token
        RefreshToken refreshToken = tokenService.validateRefreshToken(refreshTokenValue);

        // Get user and roles
        User user = userRepository.findByIdAndIsDeletedFalse(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        String primaryRole = roles.isEmpty() ? "EMPLOYEE" : roles.get(0);

        // Generate new access token
        String newAccessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), primaryRole);

        // Set new access token cookie
        cookieUtil.addAccessTokenCookie(response, newAccessToken);
    }

    public UserInfoResponse getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        return userMapper.toUserInfoResponse(user, roles);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, HttpServletResponse response) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        // Validate old password
        if (!passwordService.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(MessageConstants.INVALID_OLD_PASSWORD);
        }

        // Check password history
        passwordService.validateNotReused(user, request.getNewPassword());

        // Save old password to history
        passwordService.saveToHistory(user);

        // Update password
        user.setPasswordHash(passwordService.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        tokenService.revokeAllRefreshTokens(userId);

        // Clear cookies (force re-login)
        cookieUtil.clearAllAuthCookies(response);

        // Publish event
        eventPublisher.publishEvent(new PasswordChangedEvent(this, userId, user.getEmail()));

        log.info("Password changed for user: {}", user.getEmail());
    }

    private void handleFailedLogin(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);

        if (user.getFailedAttempts() >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusNanos(lockoutDurationMs * 1_000_000L));
            log.warn("Account locked for user: {} due to {} failed attempts", user.getEmail(), maxFailedAttempts);
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}
