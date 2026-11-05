package com.veth007.hotelmanagement.model.request.Google;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

// GoogleTokenRequest.java
@Builder
@Data
public class GoogleTokenRequest {
    @NotBlank
    private String idToken;  // Google ID token from mobile app
}
