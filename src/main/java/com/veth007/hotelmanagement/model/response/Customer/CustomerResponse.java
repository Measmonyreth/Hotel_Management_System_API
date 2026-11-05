package com.veth007.hotelmanagement.model.response.Customer;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse implements Serializable {
    private Long id;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String picture;
    private String provider;
    private String providerId;
    private String phone;
    private boolean emailVerified;
    private String role;
    private String status;
    private String nationality;
    private String city_or_province;
    private String country;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    private VipCustomerResponse vipCustomer;

    static public CustomerResponse fromEntity(CustomerEntity customer) {
        return fromEntity(customer, false);
    }

    static public CustomerResponse fromEntity(CustomerEntity customer, Boolean includeVip) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .picture(customer.getPicture())
                .provider(customer.getProvider().name())
                .providerId(customer.getProviderId())
                .emailVerified(customer.isEmailVerified())
                .phone(customer.getPhone())
                .role(customer.getRole().name())
                .status(customer.getStatus().name())
                .nationality(customer.getNationality())
                .city_or_province(customer.getCity_or_province())
                .country(customer.getCountry())
                .createdDate(customer.getCreatedDate())
                .lastModifiedDate(customer.getLastModifiedDate())
                .deletedDate(customer.getDeletedDate())
                .createBy(customer.getCreatedBy())
                .lastModifiedBy(customer.getLastModifiedBy())
                .deletedBy(customer.getDeleted_by())
                .vipCustomer(includeVip && customer.getVipCustomer() != null ? VipCustomerResponse.fromEntity(customer.getVipCustomer()) : null)
                .build();
    }
}
