package com.iloveshopping.security.oauth2;

import com.iloveshopping.security.UserPrincipal;
import com.iloveshopping.security.jwt.JwtTokenProvider;
import com.iloveshopping.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 authentication success handler.
 * Generates tokens and redirects to the frontend with tokens.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = tokenProvider.generateAccessToken(userPrincipal.getId(), userPrincipal.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(
            userPrincipal.getId(), 
            getClientIpAddress(request)
        );

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build().toUriString();

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
