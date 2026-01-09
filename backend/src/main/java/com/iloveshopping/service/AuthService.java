package com.iloveshopping.service;

import com.iloveshopping.dto.request.LoginRequest;
import com.iloveshopping.dto.request.NewPasswordRequest;
import com.iloveshopping.dto.request.PasswordResetRequest;
import com.iloveshopping.dto.request.RegisterRequest;
import com.iloveshopping.dto.response.AuthResponse;
import com.iloveshopping.dto.response.UserResponse;
import com.iloveshopping.entity.Role;
import com.iloveshopping.entity.User;
import com.iloveshopping.exception.BadRequestException;
import com.iloveshopping.repository.RoleRepository;
import com.iloveshopping.repository.UserRepository;
import com.iloveshopping.security.jwt.JwtTokenProvider;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication service handling registration, login, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RecaptchaService recaptchaService;
    private final EmailService emailService;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(HashingAlgorithm.SHA1),
            new SystemTimeProvider()
    );

    /**
     * Register a new user account.
     */
    @Transactional
    public UserResponse register(RegisterRequest request, String ipAddress) {
        // Verify reCAPTCHA
        if (!recaptchaService.verify(request.getRecaptchaToken())) {
            throw new BadRequestException("reCAPTCHA verification failed");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Get default user role
        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default user role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();

        user.addRole(userRole);
        user = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user);

        log.info("New user registered: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Authenticate user and generate tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is locked
        if (!user.getAccountNonLocked()) {
            throw new BadRequestException("Account is locked. Please contact support.");
        }

        // Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }

            // Check 2FA
            if (user.getTwoFactorEnabled()) {
                if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isBlank()) {
                    return AuthResponse.requiresTwoFactor();
                }

                if (!verifyTwoFactorCode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                    throw new BadRequestException("Invalid 2FA code");
                }
            }

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = refreshTokenService.createRefreshToken(user.getId(), ipAddress);

            log.info("User logged in: {}", user.getEmail());

            return AuthResponse.of(
                    accessToken,
                    refreshToken,
                    jwtTokenProvider.getAccessTokenExpiration(),
                    UserResponse.fromEntity(user)
            );

        } catch (BadCredentialsException e) {
            // Increment failed login attempts
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock account after 5 failed attempts
            if (attempts >= 5) {
                user.setAccountNonLocked(false);
                log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
            }

            userRepository.save(user);
            throw e;
        }
    }

    /**
     * Logout user by revoking refresh token.
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
        log.info("User logged out");
    }

    /**
     * Logout from all devices.
     */
    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("User logged out from all devices: {}", userId);
    }

    /**
     * Request password reset.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase())
                .ifPresent(user -> {
                    emailService.sendPasswordResetEmail(user);
                    log.info("Password reset requested for: {}", user.getEmail());
                });
        // Don't reveal if email exists or not
    }

    /**
     * Reset password with token.
     */
    @Transactional
    public void resetPassword(NewPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Verify token and get user
        User user = emailService.verifyPasswordResetToken(request.getToken());

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        userRepository.save(user);

        // Revoke all existing tokens
        refreshTokenService.revokeAllUserTokens(user.getId());

        log.info("Password reset completed for: {}", user.getEmail());
    }

    /**
     * Enable 2FA for user.
     */
    @Transactional
    public String enableTwoFactor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String secret = secretGenerator.generate();
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        return secret;
    }

    /**
     * Confirm and activate 2FA.
     */
    @Transactional
    public void confirmTwoFactor(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new BadRequestException("2FA not initialized");
        }

        if (!verifyTwoFactorCode(user.getTwoFactorSecret(), code)) {
            throw new BadRequestException("Invalid 2FA code");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        log.info("2FA enabled for user: {}", user.getEmail());
    }

    /**
     * Disable 2FA for user.
     */
    @Transactional
    public void disableTwoFactor(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.getTwoFactorEnabled()) {
            throw new BadRequestException("2FA is not enabled");
        }

        if (!verifyTwoFactorCode(user.getTwoFactorSecret(), code)) {
            throw new BadRequestException("Invalid 2FA code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        log.info("2FA disabled for user: {}", user.getEmail());
    }

    private boolean verifyTwoFactorCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
