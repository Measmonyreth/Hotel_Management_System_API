package com.veth007.hotelmanagement.config.Handle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.veth007.hotelmanagement.config.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            if (email == null) {
                log.error("OAuth2: Email is null from OAuth2User");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Email not found in OAuth2 user info");
                return;
            }

            log.info("OAuth2 success: email={}, frontendUrl={}", email, frontendUrl);

            String accessToken = jwtUtil.generateAccessTokenFromEmail(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
                log.error("OAuth2: frontendUrl is not configured");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Frontend URL not configured");
                return;
            }

            // Redirect to frontend with tokens in query params
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/oauth2/callback")
                    .queryParam("token", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            log.info("OAuth2 redirect URL: {}", redirectUrl);
            clearAuthenticationAttributes(request); // ✅ clean up session
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } catch (Exception e) {
            log.error("OAuth2 authentication success handler error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }
    
}
