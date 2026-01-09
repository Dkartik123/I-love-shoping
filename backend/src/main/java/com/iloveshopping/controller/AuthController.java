package com.iloveshopping.controller;

import com.iloveshopping.dto.request.*;
import com.iloveshopping.dto.response.ApiResponse;
import com.iloveshopping.dto.response.AuthResponse;
import com.iloveshopping.dto.response.UserResponse;
import com.iloveshopping.security.UserPrincipal;
import com.iloveshopping.service.AuthService;
import com.iloveshopping.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for user registration, login, and token management.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        
        String ipAddress = getClientIpAddress(servletRequest);
        UserResponse user = authService.register(request, ipAddress);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please check your email to verify your account.", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        
        String ipAddress = getClientIpAddress(servletRequest);
        AuthResponse response = authService.login(request, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest servletRequest) {
        
        String ipAddress = getClientIpAddress(servletRequest);
        RefreshTokenService.RefreshResult result = refreshTokenService.refreshAccessToken(
                request.getRefreshToken(), ipAddress);
        
        AuthResponse response = AuthResponse.of(
                result.accessToken(),
                result.refreshToken(),
                900000, // 15 minutes
                UserResponse.fromEntity(result.user())
        );
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user by revoking refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody TokenRefreshRequest request) {
        
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        authService.logoutAll(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("If an account exists with that email, a password reset link has been sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody NewPasswordRequest request) {
        
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully"));
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "Enable two-factor authentication")
    public ResponseEntity<ApiResponse<String>> enableTwoFactor(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        String secret = authService.enableTwoFactor(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("2FA secret generated. Please confirm with a code.", secret));
    }

    @PostMapping("/2fa/confirm")
    @Operation(summary = "Confirm and activate 2FA with code")
    public ResponseEntity<ApiResponse<Void>> confirmTwoFactor(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam String code) {
        
        authService.confirmTwoFactor(currentUser.getId(), code);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled successfully"));
    }

    @PostMapping("/2fa/disable")
    @Operation(summary = "Disable two-factor authentication")
    public ResponseEntity<ApiResponse<Void>> disableTwoFactor(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam String code) {
        
        authService.disableTwoFactor(currentUser.getId(), code);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        return ResponseEntity.ok(ApiResponse.success(currentUser));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
