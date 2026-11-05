package com.veth007.hotelmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.exception.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class OAuth2CallbackController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> home() {
        log.info("Home page API requested");
        return ResponseEntity.ok(ApiResponse.success("Welcome to Hotel Management API" ));
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> login() {
        log.info("Login endpoint requested - Use /api/customers/login instead");
        return ResponseEntity.ok( ApiResponse.success( "Use POST /api/customers/login for customer authentication"));
    }


    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> dashboard() {
        log.info("Dashboard endpoint requested - Not available in API mode");
        return ResponseEntity.badRequest().body(ApiResponse.error("Dashboard not available in API mode. Check /api/admin/** endpoints", 400));
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<ApiResponse<Object>> oauth2Callback(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "refreshToken", required = false) String refreshToken,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) {

        log.info("OAuth2 callback received");

        if (token != null && refreshToken != null) {
            log.info("Tokens received successfully");
            return ResponseEntity.ok(ApiResponse.success("OAuth2 authentication successful"));
        } else if (error != null) {
            log.error("OAuth2 error: {} - {}", error, errorDescription);
            return ResponseEntity.badRequest().body(ApiResponse.error("OAuth2 error: " + error + " - " + errorDescription, 400));
        } else {
            log.warn("OAuth2 callback: missing tokens and error");
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid OAuth2 callback - missing tokens", 400));
        }
    }
}
