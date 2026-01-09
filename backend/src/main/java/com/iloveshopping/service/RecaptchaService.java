package com.iloveshopping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service for verifying Google reCAPTCHA tokens.
 */
@Slf4j
@Service
public class RecaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verify reCAPTCHA token with Google.
     */
    @SuppressWarnings("unchecked")
    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Empty reCAPTCHA token provided");
            return false;
        }

        // Skip verification if secret key is not configured (for development)
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("reCAPTCHA secret key not configured, skipping verification");
            return true;
        }

        try {
            String url = String.format("%s?secret=%s&response=%s", verifyUrl, secretKey, token);
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                log.debug("reCAPTCHA verification successful");
                return true;
            }

            log.warn("reCAPTCHA verification failed: {}", response);
            return false;

        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA", e);
            return false;
        }
    }
}
