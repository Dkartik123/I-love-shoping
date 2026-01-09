package com.iloveshopping.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iloveshopping.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for handling authentication errors.
 * Returns 401 Unauthorized responses with proper JSON formatting.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response, 
                         AuthenticationException authException) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiResponse<Object> apiResponse = ApiResponse.error(
            "Unauthorized",
            "You need to login to access this resource"
        );

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
