package com.iloveshopping.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token refresh request DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
