package com.veth007.hotelmanagement.model.request.Discount;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {
    private String description;
    @NotBlank(message = "Type is required")
    private String type;
    @NotBlank(message = "Valid from is required")
    private String valid_from;
    @NotBlank(message = "Valid until is required")
    private String valid_until;
}
