package com.iloveshopping.service;

import com.iloveshopping.entity.RefreshToken;
import com.iloveshopping.entity.User;
import com.iloveshopping.exception.BadRequestException;
import com.iloveshopping.repository.RefreshTokenRepository;
import com.iloveshopping.repository.UserRepository;
import com.iloveshopping.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing refresh tokens with rotation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Create a new refresh token for a user.
     */
    @Transactional
    public String createRefreshToken(UUID userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String tokenString = jwtTokenProvider.generateRefreshToken(userId);
        long expirationMs = jwtTokenProvider.getRefreshTokenExpiration();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .createdByIp(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", userId);

        return tokenString;
    }

    /**
     * Refresh the access token using a refresh token.
     * Implements token rotation - each refresh token can only be used once.
     */
    @Transactional
    public RefreshResult refreshAccessToken(String refreshTokenString, String ipAddress) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        // Check if token is active
        if (!refreshToken.isActive()) {
            // If someone tries to use a revoked token, revoke all user tokens (potential token theft)
            if (refreshToken.getRevoked()) {
                log.warn("Attempted use of revoked refresh token. Revoking all tokens for user: {}", 
                        refreshToken.getUser().getId());
                refreshTokenRepository.revokeAllUserTokens(refreshToken.getUser());
            }
            throw new BadRequestException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        // Revoke current token (rotation)
        refreshToken.setRevoked(true);

        // Create new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = createRefreshToken(user.getId(), ipAddress);

        // Mark old token as replaced
        refreshToken.setReplacedBy(newRefreshToken);
        refreshTokenRepository.save(refreshToken);

        log.debug("Rotated refresh token for user: {}", user.getId());

        return new RefreshResult(newAccessToken, newRefreshToken, user);
    }

    /**
     * Revoke a specific refresh token.
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.revokeToken(token);
        log.debug("Revoked refresh token");
    }

    /**
     * Revoke all refresh tokens for a user (logout from all devices).
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        refreshTokenRepository.revokeAllUserTokens(user);
        log.debug("Revoked all refresh tokens for user: {}", userId);
    }

    /**
     * Clean up expired tokens periodically.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    public record RefreshResult(String accessToken, String refreshToken, User user) {}
}
