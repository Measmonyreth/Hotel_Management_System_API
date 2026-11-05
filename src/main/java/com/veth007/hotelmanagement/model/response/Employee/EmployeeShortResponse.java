package com.veth007.hotelmanagement.model.response.Employee;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeShortResponse {
    private Long id;
    private String name;

    public static EmployeeShortResponse fromEntity(EmployeeEntity e) {
        if (e == null) return null;
        return EmployeeShortResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }
}
