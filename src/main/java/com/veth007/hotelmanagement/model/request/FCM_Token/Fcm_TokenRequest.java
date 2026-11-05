package com.veth007.hotelmanagement.model.request.FCM_Token;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fcm_TokenRequest {
    
    @NotBlank(message = "Token cannot be blank")
    private String token;
    
    private Long customerId; // ID of the user to whom this token belongs
    private String deviceType; // android, ios, web
}