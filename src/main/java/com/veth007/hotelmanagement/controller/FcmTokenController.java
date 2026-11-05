package com.veth007.hotelmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.veth007.hotelmanagement.service.FCM_Token.FCM_TokenService;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.FCM_Token.Fcm_TokenRequest;
import com.veth007.hotelmanagement.model.request.FCM_Token.UpdateFcm_TokenRequest;
import com.veth007.hotelmanagement.model.response.FCM_Token.Fcm_TokenResponse;
@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
@Slf4j
public class FcmTokenController {
    
    private final FCM_TokenService fcmTokenService;
    
    /**
     * Save or update FCM token for current user
     * POST /api/fcm-tokens
     */
    @PostMapping
 //   @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Fcm_TokenResponse>> saveToken(
        @Valid @RequestBody Fcm_TokenRequest request
    //    @RequestAttribute("userId") Long userId
    ) {
        
        log.info("Received FCM token save request");
        Fcm_TokenResponse response = fcmTokenService.saveToken(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all tokens for current user
     * GET /api/fcm-tokens
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF') or hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<List<Fcm_TokenResponse>>> getUserTokens(
        @RequestAttribute("userId") Long userId) {
        
        log.info("Fetching all tokens for user: {}", userId);
        List<Fcm_TokenResponse> tokens = fcmTokenService.getUserTokens(userId);

        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
    
    /**
     * Get specific token details by token string
     * GET /api/fcm-tokens/{token}
     */
    @GetMapping("/{token}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Fcm_TokenResponse>> getTokenDetails(
        @PathVariable String token) {
        
        log.info("Fetching token details: {}", token);
        Fcm_TokenResponse response = fcmTokenService.getTokenDetails(token);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    public ResponseEntity<ApiResponse<List<Fcm_TokenResponse>>> getAllTokens() {
        log.info("Admin fetching all FCM tokens");
        List<Fcm_TokenResponse> tokens = fcmTokenService.getAll();
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
    
    /**
     * Update token status (activate/deactivate)
     * PATCH /api/fcm-tokens/status
     */
    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<Fcm_TokenResponse>> updateTokenStatus(
        @Valid @RequestBody UpdateFcm_TokenRequest request) {
        
        log.info("Updating token status: {}", request.getToken());
        Fcm_TokenResponse response = fcmTokenService.updateTokenStatus(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/update-user")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<Fcm_TokenResponse>> updateTokenUserId(
        @Valid @RequestBody UpdateFcm_TokenRequest request,@AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating token user ID: {}", request.getToken());
        String email = userDetails.getUsername(); // Assuming username is email
        Fcm_TokenResponse response = fcmTokenService.updateTokenUserId(request, email);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Remove a specific token (logout from device)
     * DELETE /api/fcm-tokens/{token}
     */
    @DeleteMapping("/{token}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeToken(@PathVariable String token) {
        
        log.info("Removing token: {}", token);
        fcmTokenService.removeToken(token);
        
        return ResponseEntity.ok(ApiResponse.ok("Token removed successfully"));
    }
    
    // /**
    //  * Remove all tokens for current user (logout from all devices)
    //  * DELETE /api/fcm-tokens
    //  */
    // @DeleteMapping
    // @PreAuthorize("hasAnyRole( 'ADMIN')")
    // public ResponseEntity<ApiResponse<Void>> removeAllUserTokens(
    //     @RequestAttribute("userId") Long userId) {
        
    //     log.info("Removing all tokens for user: {}", userId);
    //     fcmTokenService.removeAllUserTokens(userId);
        
    //     return ResponseEntity.ok(ApiResponse.ok("All tokens removed successfully"));
    // }
    
    /**
     * Admin endpoint: Remove all tokens for a user
     * DELETE /api/fcm-tokens/admin/{userId}
     */
    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminRemoveAllUserTokens(
        @PathVariable Long userId) {
        
        log.info("Admin removing all tokens for user: {}", userId);
        fcmTokenService.removeAllUserTokens(userId);
        
        return ResponseEntity.ok(ApiResponse.ok("All user tokens removed successfully"));
    }
}
