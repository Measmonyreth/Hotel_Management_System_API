package com.veth007.hotelmanagement.config;

import java.io.IOException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veth007.hotelmanagement.exception.BusinessException;
import com.veth007.hotelmanagement.service.CustomerDetailsService;
import com.veth007.hotelmanagement.service.EmployeeDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final EmployeeDetailsService employeeDetailsService;
    private final CustomerDetailsService customerDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil,
            @Lazy EmployeeDetailsService employeeDetailsService,
            @Lazy CustomerDetailsService customerDetailsService) {
        this.jwtUtil = jwtUtil;
        this.employeeDetailsService = employeeDetailsService;
        this.customerDetailsService = customerDetailsService;
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip JWT processing for OAuth2 callback paths
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/oauth2/") || requestPath.startsWith("/login/oauth2/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);
            String userType = jwtUtil.extractUserType(token); // "EMPLOYEE" or "CUSTOMER"

            System.out.println("=== JWT DEBUG ===");
            System.out.println("Username: " + username);
            System.out.println("UserType: " + userType);
            System.out.println("=================");

            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

            if (username != null &&
                    (existingAuth == null ||
                     existingAuth instanceof AnonymousAuthenticationToken ||
                     !(existingAuth.getPrincipal() instanceof UserDetails) ||
                     !existingAuth.getName().equals(username))) {

                UserDetails userDetails;
                if ("EMPLOYEE".equals(userType)) {
                    userDetails = employeeDetailsService.loadUserByUsername(username);
                } else if ("CUSTOMER".equals(userType)) {
                    userDetails = customerDetailsService.loadUserByUsername(username);
                } else {
                    throw new BusinessException("Unknown user type: " + userType);
                }
                System.out.println("UserDetails type: " + userDetails.getClass().getName());

                if (userDetails != null && jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
        } catch (JwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        }
    }

    // private String resolveUserType(String token, String username) {
    //     String userType = jwtUtil.extractUserType(token);
    //     if (userType != null && !userType.isBlank()) {
    //         return userType;
    //     }

    //     try {
    //         employeeDetailsService.loadUserByUsername(username);
    //         return "EMPLOYEE";
    //     } catch (UsernameNotFoundException ignored) {
    //         // Try customer fallback
    //     }

    //     try {
    //         customerDetailsService.loadUserByUsername(username);
    //         return "CUSTOMER";
    //     } catch (UsernameNotFoundException ignored) {
    //         return "CUSTOMER";
    //     }
    // }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", String.valueOf(status));
        errorResponse.put("message", message);
        errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    // private UserDetails loadUser(String username) {
    // try {
    // return employeeDetailsService.loadUserByUsername(username);
    // } catch (UsernameNotFoundException e) {
    // return customerDetailsService.loadUserByUsername(username);
    // }
    // }
}
