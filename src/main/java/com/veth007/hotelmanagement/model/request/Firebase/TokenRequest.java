package com.veth007.hotelmanagement.model.request.Firebase;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;

@Builder
@Data
public class TokenRequest {
    private String userId;
    private String fcmToken;
}
