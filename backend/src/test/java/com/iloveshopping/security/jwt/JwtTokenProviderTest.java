package com.iloveshopping.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JWT Token Provider.
 * Tests token generation, validation, and expiration.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "test-secret-key-must-be-at-least-32-characters-long-for-hs256";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        String token = jwtTokenProvider.generateRefreshToken(userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // When
        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should extract email from access token")
    void shouldExtractEmailFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // When
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should identify access token type")
    void shouldIdentifyAccessTokenType() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "test@example.com");

        // When
        String tokenType = jwtTokenProvider.getTokenType(token);

        // Then
        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    @DisplayName("Should identify refresh token type")
    void shouldIdentifyRefreshTokenType() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateRefreshToken(userId);

        // When
        String tokenType = jwtTokenProvider.getTokenType(token);

        // Then
        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    @DisplayName("Should invalidate tampered token")
    void shouldInvalidateTamperedToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "test@example.com");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        // When & Then
        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Should invalidate empty token")
    void shouldInvalidateEmptyToken() {
        // When & Then
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void shouldInvalidateMalformedToken() {
        // When & Then
        assertThat(jwtTokenProvider.validateToken("not.a.valid.jwt.token")).isFalse();
        assertThat(jwtTokenProvider.validateToken("random-string")).isFalse();
    }

    @Test
    @DisplayName("Should get expiration from token")
    void shouldGetExpirationFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "test@example.com");

        // When
        java.util.Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("Access token should not be expired immediately after creation")
    void accessTokenShouldNotBeExpiredImmediately() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "test@example.com");

        // When & Then
        assertThat(jwtTokenProvider.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("Should generate unique tokens for same user")
    void shouldGenerateUniqueTokens() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token1 = jwtTokenProvider.generateAccessToken(userId, email);
        String token2 = jwtTokenProvider.generateAccessToken(userId, email);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }
}
