package com.veth007.hotelmanagement.model.request.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAddressRequest {
    private String village;
    private String commun;
    private String city_or_province;
    private String country;
    private Long employeeId;
}
