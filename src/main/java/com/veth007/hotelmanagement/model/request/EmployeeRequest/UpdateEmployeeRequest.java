package com.veth007.hotelmanagement.model.request.EmployeeRequest;

import com.veth007.hotelmanagement.model.request.Address.UpdateAddressRequest;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmployeeRequest {
    private String name;
    private String username;
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers and be 7-15 digits")
    private String phone;
    private String role;
    private String status;
    private UpdateAddressRequest address;
    private Long schedule;
    private String createdDate;
}
