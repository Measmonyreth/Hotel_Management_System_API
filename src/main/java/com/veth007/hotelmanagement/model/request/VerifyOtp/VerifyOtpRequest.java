package com.veth007.hotelmanagement.model.request.VerifyOtp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ===== DTO =====
public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String otpCode
) {}
