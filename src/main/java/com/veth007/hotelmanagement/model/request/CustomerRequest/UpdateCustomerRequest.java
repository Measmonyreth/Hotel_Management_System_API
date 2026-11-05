package com.veth007.hotelmanagement.model.request.CustomerRequest;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateCustomerRequest implements Serializable {
  private Long id;
    private String firstName;
    private String lastName;
    @Email(message = "Invalid email address")
    private String email;
    private String picture;
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers and be 7-15 digits")
    private String phone;
    private String role;
    private String status;
    private String nationality;
    private String city_or_province;
    private String country;
    private String createDated;
}
