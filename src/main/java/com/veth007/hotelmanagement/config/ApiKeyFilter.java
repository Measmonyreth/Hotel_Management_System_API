package com.veth007.hotelmanagement.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${spring.security.api-key}")
    private String apiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (requestPath.equals("/actuator/health")
                || requestPath.equals("/actuator/info")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader("X-API-KEY");

        boolean isApiKeyValid = apiKey != null
                && !apiKey.isBlank()
                && requestApiKey != null
                && MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                requestApiKey.getBytes(StandardCharsets.UTF_8));

        if (!isApiKeyValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
