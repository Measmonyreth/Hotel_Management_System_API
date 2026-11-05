package com.veth007.hotelmanagement.model.response;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.constant.enums.DiscountType;
import com.veth007.hotelmanagement.model.entity.DiscountEntity;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerShortResonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountResponse {
    private Long id;
    private String code;
    private String description;
    private String type;
    private Double discount_price;

    private Object vip;

    public static DiscountResponse fromEntity(DiscountEntity discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .type(DiscountType.valueOf(discount.getType().toString()).name())
                .discount_price(
                        discount.getDiscount_price() != null ? discount.getDiscount_price().doubleValue() : null)
                .vip(discount.getVip() != null
                        ? VipCustomerShortResonse.fromEntity(discount.getVip())
                        : null)
                .build();
    }
}
