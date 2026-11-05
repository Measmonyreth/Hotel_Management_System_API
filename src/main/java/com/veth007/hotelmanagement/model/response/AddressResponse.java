package com.veth007.hotelmanagement.model.response;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.AddressEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Long id;
    private String village;
    private String commun;
    private String city_or_province;
    private String country;

    public static AddressResponse fromEntity(AddressEntity address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
            .id(address.getId() )
            .village(address.getVillage())
            .commun(address.getCommun())
            .city_or_province(address.getCity_or_province())
            .country(address.getCountry())
            .build();
    }



}

