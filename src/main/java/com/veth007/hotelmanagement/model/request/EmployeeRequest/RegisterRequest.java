package com.veth007.hotelmanagement.model.request.EmployeeRequest;

import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.model.request.Address.AddressRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers and be 7-15 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    private AddressRequest address;

    private Long scheduleId; // optional — assign schedule later
}