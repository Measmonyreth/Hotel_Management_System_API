package com.veth007.hotelmanagement.model.request.CustomerRequest;

import jakarta.validation.constraints.NotBlank;

public record LoginCustomerRequest(
    @NotBlank(message = "Email is required")
    String email,
    @NotBlank(message = "Password is required")
    String password
) {}

