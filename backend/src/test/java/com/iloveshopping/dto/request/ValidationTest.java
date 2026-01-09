package com.iloveshopping.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for user input validation.
 * Tests proper handling of various input scenarios.
 */
class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Registration Validation Tests")
    class RegistrationValidationTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassWithValidData() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with invalid email")
        void shouldFailWithInvalidEmail() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("invalid-email")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        }

        @Test
        @DisplayName("Should fail with empty email")
        void shouldFailWithEmptyEmail() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should fail with weak passwords")
        @ValueSource(strings = {
                "short",           // Too short
                "nouppercase123!", // No uppercase
                "NOLOWERCASE123!", // No lowercase
                "NoNumbers!",      // No numbers
                "NoSpecial123"     // No special characters
        })
        void shouldFailWithWeakPassword(String password) {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password(password)
                    .confirmPassword(password)
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should accept strong passwords")
        @ValueSource(strings = {
                "SecurePass123!",
                "MyP@ssword123",
                "C0mpl3x!Pass",
                "Test$Password1"
        })
        void shouldAcceptStrongPassword(String password) {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password(password)
                    .confirmPassword(password)
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with empty first name")
        void shouldFailWithEmptyFirstName() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"))).isTrue();
        }

        @Test
        @DisplayName("Should fail with too short first name")
        void shouldFailWithTooShortFirstName() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("J")
                    .lastName("Doe")
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"))).isTrue();
        }

        @Test
        @DisplayName("Should fail without reCAPTCHA token")
        void shouldFailWithoutRecaptchaToken() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .recaptchaToken("")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("recaptchaToken"))).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should validate phone number format")
        @ValueSource(strings = {
                "+37253740504",
                "+14155551234",
                "1234567890123"
        })
        void shouldAcceptValidPhoneNumbers(String phone) {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .phone(phone)
                    .recaptchaToken("valid-token")
                    .build();

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Login Validation Tests")
    class LoginValidationTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassWithValidData() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("anypassword")
                    .build();

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with empty email")
        void shouldFailWithEmptyEmail() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("")
                    .password("anypassword")
                    .build();

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail with empty password")
        void shouldFailWithEmptyPassword() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("")
                    .build();

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Password Reset Validation Tests")
    class PasswordResetValidationTests {

        @Test
        @DisplayName("Should pass with valid email")
        void shouldPassWithValidEmail() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest("test@example.com");

            // When
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with invalid email")
        void shouldFailWithInvalidEmail() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest("not-an-email");

            // When
            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
        }
    }
}
