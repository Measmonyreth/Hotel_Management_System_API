package com.veth007.hotelmanagement.model.response.Customer;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerShortResponse {
    private Long id;
    private String name;
    private String customerId;

    public static CustomerShortResponse fromEntity(CustomerEntity c) {
        if (c == null) return null;
        String first = c.getFirstName() == null ? "" : c.getFirstName();
        String last = c.getLastName() == null ? "" : c.getLastName();
        String full = (first + " " + last).trim();
        return CustomerShortResponse.builder()
                .id(c.getId())
                .name(full.isEmpty() ? null : full)
                .customerId(c.getCustomerId())
                .build();
    }
}
