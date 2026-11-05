package com.veth007.hotelmanagement.model.response.FCM_Token;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.FcmTokenEntity;
import com.veth007.hotelmanagement.model.entity.TaskEnttity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fcm_TokenResponse {
    
    private Long id;
    private Map<String, Object> user; // Assuming userId is a Map for demonstration; adjust as needed
    private String token;
    private String deviceType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static Fcm_TokenResponse fromEntity(FcmTokenEntity entity) {
        return Fcm_TokenResponse.builder()
            .id(entity.getId())
            .user(customer(entity))
            .token(entity.getToken())
            .deviceType(entity.getDeviceType())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

        private static Map<String, Object> customer(FcmTokenEntity entity) {
        if (entity.getCustomer() == null) return null;
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", entity.getCustomer().getId());
        customer.put("email", entity.getCustomer().getEmail());
        return customer;
    }
}