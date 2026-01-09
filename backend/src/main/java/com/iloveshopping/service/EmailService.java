package com.iloveshopping.service;

import com.iloveshopping.entity.User;
import com.iloveshopping.exception.BadRequestException;
import com.iloveshopping.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for sending emails (verification, password reset, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@iloveshopping.com}")
    private String fromEmail;

    /**
     * Send verification email to new user.
     */
    @Async
    public void sendVerificationEmail(User user) {
        String token = generateToken();
        saveEmailVerificationToken(user.getId(), token);

        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        String subject = "Verify your email - I Love Shopping";
        String content = buildEmailTemplate(
                "Email Verification",
                "Welcome to I Love Shopping, " + user.getFirstName() + "!",
                "Please click the button below to verify your email address.",
                verificationLink,
                "Verify Email"
        );

        sendEmail(user.getEmail(), subject, content);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    /**
     * Send password reset email.
     */
    @Async
    public void sendPasswordResetEmail(User user) {
        String token = generateToken();
        savePasswordResetToken(user.getId(), token);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "Reset your password - I Love Shopping";
        String content = buildEmailTemplate(
                "Password Reset",
                "Hello " + user.getFirstName() + ",",
                "We received a request to reset your password. Click the button below to create a new password. This link will expire in 1 hour.",
                resetLink,
                "Reset Password"
        );

        sendEmail(user.getEmail(), subject, content);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    /**
     * Verify email token and activate user account.
     */
    @Transactional
    public User verifyEmailToken(String token) {
        Query query = entityManager.createNativeQuery(
                "SELECT user_id FROM email_verification_tokens WHERE token = :token AND expires_at > :now",
                UUID.class
        );
        query.setParameter("token", token);
        query.setParameter("now", LocalDateTime.now());

        try {
            UUID userId = (UUID) query.getSingleResult();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            user.setEmailVerified(true);
            userRepository.save(user);

            // Delete used token
            entityManager.createNativeQuery("DELETE FROM email_verification_tokens WHERE token = :token")
                    .setParameter("token", token)
                    .executeUpdate();

            return user;
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired verification token");
        }
    }

    /**
     * Verify password reset token.
     */
    @Transactional
    public User verifyPasswordResetToken(String token) {
        Query query = entityManager.createNativeQuery(
                "SELECT user_id FROM password_reset_tokens WHERE token = :token AND expires_at > :now AND used = false",
                UUID.class
        );
        query.setParameter("token", token);
        query.setParameter("now", LocalDateTime.now());

        try {
            UUID userId = (UUID) query.getSingleResult();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Mark token as used
            entityManager.createNativeQuery("UPDATE password_reset_tokens SET used = true WHERE token = :token")
                    .setParameter("token", token)
                    .executeUpdate();

            return user;
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired reset token");
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void saveEmailVerificationToken(UUID userId, String token) {
        entityManager.createNativeQuery(
                "INSERT INTO email_verification_tokens (id, user_id, token, expires_at) VALUES (:id, :userId, :token, :expiresAt)"
        )
                .setParameter("id", UUID.randomUUID())
                .setParameter("userId", userId)
                .setParameter("token", token)
                .setParameter("expiresAt", LocalDateTime.now().plusDays(1))
                .executeUpdate();
    }

    private void savePasswordResetToken(UUID userId, String token) {
        entityManager.createNativeQuery(
                "INSERT INTO password_reset_tokens (id, user_id, token, expires_at) VALUES (:id, :userId, :token, :expiresAt)"
        )
                .setParameter("id", UUID.randomUUID())
                .setParameter("userId", userId)
                .setParameter("token", token)
                .setParameter("expiresAt", LocalDateTime.now().plusHours(1))
                .executeUpdate();
    }

    private String buildEmailTemplate(String title, String greeting, String message, String actionUrl, String buttonText) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background-color: #4F46E5; padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 20px; color: #333; margin-bottom: 20px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .button { display: inline-block; padding: 14px 30px; background-color: #4F46E5; color: #ffffff; text-decoration: none; border-radius: 6px; font-weight: 600; }
                    .button:hover { background-color: #4338CA; }
                    .footer { background-color: #f8f8f8; padding: 20px 30px; text-align: center; color: #999; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛒 I Love Shopping</h1>
                    </div>
                    <div class="content">
                        <p class="greeting">%s</p>
                        <p class="message">%s</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">%s</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 I Love Shopping. All rights reserved.</p>
                        <p>If you didn't request this email, please ignore it.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, greeting, message, actionUrl, buttonText);
    }
}
