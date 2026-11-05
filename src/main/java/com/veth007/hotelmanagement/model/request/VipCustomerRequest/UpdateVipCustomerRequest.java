package com.veth007.hotelmanagement.model.request.VipCustomerRequest;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVipCustomerRequest implements Serializable, VipCustmerRequestBase {
        private String code;
    private String type_vip;
    private Integer points;
    private Double totalSpent;
    private String validFrom;
    private String validUntil;
    private String status;
    private double percentage_discount;
    private Long customerId;
    private String createdDate;
}
