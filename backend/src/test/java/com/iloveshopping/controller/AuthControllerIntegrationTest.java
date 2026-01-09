package com.iloveshopping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iloveshopping.dto.request.LoginRequest;
import com.iloveshopping.dto.request.RegisterRequest;
import com.iloveshopping.dto.response.AuthResponse;
import com.iloveshopping.dto.response.UserResponse;
import com.iloveshopping.service.AuthService;
import com.iloveshopping.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Authentication API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .recaptchaToken("valid-token")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .emailVerified(false)
                .twoFactorEnabled(false)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(authService.register(any(), any())).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @DisplayName("Should fail registration with invalid email")
    void shouldFailRegistrationWithInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .recaptchaToken("valid-token")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("Should fail registration with weak password")
    void shouldFailRegistrationWithWeakPassword() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("weak")
                .confirmPassword("weak")
                .firstName("John")
                .lastName("Doe")
                .recaptchaToken("valid-token")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        AuthResponse authResponse = AuthResponse.of(
                "access-token",
                "refresh-token",
                900000,
                UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("test@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );

        when(authService.login(any(), any())).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Should fail login with invalid credentials")
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(authService.login(any(), any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should require 2FA when enabled")
    void shouldRequire2FAWhenEnabled() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        when(authService.login(any(), any())).thenReturn(AuthResponse.requiresTwoFactor());

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiresTwoFactor").value(true));
    }
}
