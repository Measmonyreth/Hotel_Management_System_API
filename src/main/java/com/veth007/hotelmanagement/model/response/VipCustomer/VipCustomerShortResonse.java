package com.veth007.hotelmanagement.model.response.VipCustomer;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.VipCustomerEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipCustomerShortResonse {
        private Long id;
    private String code;
    private String type_vip;

    public static VipCustomerShortResonse fromEntity(VipCustomerEntity vipCustomer) {
        return VipCustomerShortResonse.builder()
                .id(vipCustomer.getId())
                .code(vipCustomer.getCode())
                .type_vip(vipCustomer.getTypeVip().name())
                .build();
    }
}
