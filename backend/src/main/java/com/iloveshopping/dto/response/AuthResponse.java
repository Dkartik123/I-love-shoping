package com.iloveshopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response containing tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;
    private boolean requiresTwoFactor;

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .requiresTwoFactor(false)
                .build();
    }

    public static AuthResponse requiresTwoFactor() {
        return AuthResponse.builder()
                .requiresTwoFactor(true)
                .build();
    }
}
