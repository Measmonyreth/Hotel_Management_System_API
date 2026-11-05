package com.veth007.hotelmanagement.controller;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.VerifyOtp.VerifyOtpRequest;
import com.veth007.hotelmanagement.service.EmailVerifire.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * Send verification email
     * @param email Gmail address
     * @return JSON response
     */
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<String>> send(@RequestParam String email) {
        return ResponseEntity.ok(emailVerificationService.sendVerification(email));
    }

    /**
     * Verify email token from link
     * @param token Verification token from email link
     * @return JSON response
     */
    @PostMapping("/verify-otp")
public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    return ResponseEntity.ok(emailVerificationService.verifyOtp(request.email(), request.otpCode()));
}

    /**
     * Check email verification status
     * @param email Gmail address
     * @return JSON response
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> status(@RequestParam String email) {
        return ResponseEntity.ok(emailVerificationService.checkStatus(email));
    }
}
