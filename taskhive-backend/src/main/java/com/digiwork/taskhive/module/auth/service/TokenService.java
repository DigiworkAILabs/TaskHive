package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.module.auth.exception.InvalidTokenException;
import com.digiwork.taskhive.module.auth.exception.TokenExpiredException;
import com.digiwork.taskhive.module.auth.model.RefreshToken;
import com.digiwork.taskhive.module.auth.repository.RefreshTokenRepository;
import com.digiwork.taskhive.module.auth.security.JwtConfig;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(UUID userId, String email, String role, Long tokenVersion) {
        return jwtTokenProvider.generateAccessToken(userId, email, role, tokenVersion);
    }

    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public UUID getUserIdFromAccessToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    public String getEmailFromAccessToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    /**
     * Generate a raw UUID refresh token, store its SHA-256 hash in the DB
     */
    @Transactional
    public String generateRefreshToken(UUID userId) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(jwtConfig.getRefreshTokenExpiry() * 1_000_000L))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    /**
     * Validate a refresh token: hash the raw token, look up in DB, check expiry
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(MessageConstants.INVALID_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(MessageConstants.TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllRefreshTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * SHA-256 hash of a token for secure storage
     */
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
