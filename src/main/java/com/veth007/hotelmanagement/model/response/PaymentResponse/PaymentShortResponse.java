package com.veth007.hotelmanagement.model.response.PaymentResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;

import lombok.AllArgsConstructor;

@Data
@Builder 
@RequiredArgsConstructor
@AllArgsConstructor
public class PaymentShortResponse {
    private Long id;
   private String code;

   public static PaymentShortResponse fromEntity(PaymentEntity paymentEntity) {
       return PaymentShortResponse.builder()
               .id(paymentEntity.getId())
               .code(paymentEntity.getCode())
               .build();
   }
}
