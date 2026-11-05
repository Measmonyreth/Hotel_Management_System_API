package com.veth007.hotelmanagement.model.response.VipCustomer;

import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.VipCustomerEntity;
import com.veth007.hotelmanagement.model.response.Customer.CustomerShortResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipCustomerResponse {
    private Long id;
    private String code;
    private String type_vip;
    private Double points;
    private Double totalSpent;
    private String validFrom;
    private String validUntil;
    private String status;
    private double percentage_discount;
    private Object customer;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    static public VipCustomerResponse fromEntity(VipCustomerEntity vipCustomer) {
        return VipCustomerResponse.builder()
                .id( vipCustomer.getId())
                .code(vipCustomer.getCode())
                .type_vip(vipCustomer.getTypeVip().name())
                .points(vipCustomer.getPoints())
                .totalSpent(vipCustomer.getTotalSpent())
                .validFrom(vipCustomer.getValidFrom().toString())
                .validUntil(vipCustomer.getValidUntil().toString())
                .status(vipCustomer.getStatus().name())
                .percentage_discount(vipCustomer.getPercentage_discount())
                .customer(CustomerShortResponse.fromEntity(vipCustomer.getCustomer()))
                .createdDate(vipCustomer.getCreatedDate())
                .lastModifiedDate(vipCustomer.getLastModifiedDate())
                .deletedDate(vipCustomer.getDeletedDate())
                .createBy(vipCustomer.getCreatedBy())
                .lastModifiedBy(vipCustomer.getLastModifiedBy())
                .deletedBy(vipCustomer.getDeleted_by())
                .build();
    }
}