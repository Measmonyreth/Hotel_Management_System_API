package com.veth007.hotelmanagement.model.response.VipCustomer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
@JsonPropertyOrder({ "totalVipCustomers", "activeVipCustomers",
        "inactiveVipCustomers", "deletedVipCustomers", "silver", "gold", "platinum", "diamond",
        "vipCustomerResponses" })
public class VipListCustomerResponse {
    private int totalVipCustomers;
    private int activeVipCustomers;
    private int expiredVipCustomers;
    private int deletedVipCustomers;
    private int silver;
    private int gold;
    private int platinum;
    private int diamond;
    private List<VipCustomerResponse> vipCustomerResponses;
}
