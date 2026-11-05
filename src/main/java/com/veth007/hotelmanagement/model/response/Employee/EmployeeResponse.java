package com.veth007.hotelmanagement.model.response.Employee;

import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
import com.veth007.hotelmanagement.model.response.AddressResponse;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
        private Long id;
        private String employeeId;
        private String name;
        private String username;
        private String phone;
        private String role;
        private String status;
        private Object shift;
        private Object address;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private LocalDateTime deletedDate;
        private String createBy;
        private String lastModifiedBy;
        private String deletedBy;

        public static EmployeeResponse fromEntity(EmployeeEntity e) {
                return fromEntity(e, null, null);
        }

        public static EmployeeResponse fromEntity(EmployeeEntity e, Boolean includeShift, Boolean includeAddress) {
                return EmployeeResponse.builder()
                                .employeeId(e.getEmployeeId())

                                .id(e.getId())
                                .name(e.getName())
                                .username(e.getUsername())
                                .phone(e.getPhone())
                                .role(e.getRole().name())
                                .status(e.getStatus().name())
                                .shift(e.getShift() != null
                                                ? (Boolean.TRUE.equals(includeShift)
                                                                ? ShiftResponse.fromEntity(e.getShift())
                                                                : e.getShift().getId())
                                                : null)
                                .address(e.getAddress() != null
                                                ? (Boolean.TRUE.equals(includeAddress)
                                                                ? AddressResponse.fromEntity(e.getAddress())
                                                                : e.getAddress().getId())
                                                : null)
                                .createdDate(e.getCreatedDate())
                                .lastModifiedDate(e.getLastModifiedDate())
                                .deletedDate(e.getDeletedDate())
                                .createBy(e.getCreatedBy())
                                .lastModifiedBy(e.getLastModifiedBy())
                                .deletedBy(e.getDeleted_by())
                                .build();
        }
}