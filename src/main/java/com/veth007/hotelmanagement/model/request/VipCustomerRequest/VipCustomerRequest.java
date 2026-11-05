package com.veth007.hotelmanagement.model.request.VipCustomerRequest;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipCustomerRequest implements VipCustmerRequestBase, Serializable {
    @NotBlank(message = "Type vip is required")
    private String type_vip;
    @NotBlank(message = "Points is required")
    private Integer points;
    @NotBlank(message = "Total spent is required")
    private Double totalSpent;
    @NotBlank(message = "Valid from is required")
    private String validFrom;
    @NotBlank(message = "Valid until is required")
    private String validUntil;

    @NotBlank(message = "Percentage discount is required")
    private double percentage_discount;
    @NotBlank(message = "Customer id is required")
    private Long customerId;
}
