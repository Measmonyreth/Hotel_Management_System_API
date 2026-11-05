package com.veth007.hotelmanagement.model.response;

import java.time.LocalDateTime;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import lombok.Builder;

public record AuthCustomerResponse(
        String token,
        String refreshToken,
        String email,
        String role) {
        @Builder
        public record UserInfo(
                String id,
                String email,
                String name,
                String phone,
                String role,
                String picture,
                boolean emailVerified,
                String provider,
                String status,
                String nationality,
                String city_or_province,
                String country,
                VipCustomerResponse vipCustomer,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {}
}
