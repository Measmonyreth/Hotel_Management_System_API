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
public class UpdateFcm_TokenRequest {
    
    @NotBlank(message = "Token cannot be blank")
    private String token;
    
    private Boolean isActive;
}