package com.veth007.hotelmanagement.service.EmailVerifire;

import com.veth007.hotelmanagement.exception.ApiResponse;

public interface EmailVerificationService {
    ApiResponse<String> sendVerification(String email);
    ApiResponse<String> verifyOtp(String email, String otpCode);
    ApiResponse<String> checkStatus(String email);
}
