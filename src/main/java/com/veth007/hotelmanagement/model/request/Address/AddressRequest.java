package com.veth007.hotelmanagement.model.request.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Village is required")
    @Size(min = 3, max = 50, message = "Village must be 3-50 characters")
    private String village;
    @NotBlank(message = "Commun is required")
    @Size(min = 3, max = 50, message = "Commun must be 3-50 characters")
    private String commun;
    @NotBlank(message = "City or Province is required")
    @Size(min = 3, max = 50, message = "City or Province must be 3-50 characters")
    private String city_or_province;
    @NotBlank(message = "Country is required")
    @Size(min = 3, max = 50, message = "Country must be 3-50 characters")
    private String country;
  //  private Long employeeId;

}
