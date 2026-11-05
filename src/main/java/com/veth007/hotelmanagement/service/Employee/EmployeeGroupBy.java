package com.veth007.hotelmanagement.service.Employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.UpdateEmployeeRequest;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeGroupBy {
    STATUS (dto -> dto.getStatus() != null ? dto.getStatus() : "N/A", 
    (filter, value) -> filter.setStatus(value)), 
    ROLE (dto -> dto.getRole() != null ? dto.getRole() : "N/A", 
    (filter, value) -> filter.setRole(value)),
    SHIFT (dto -> dto.getShift() != null ? dto.getShift().toString() : "N/A", 
    (filter, value) -> {
        if (!"N/A".equals(value)) {
            filter.setSchedule(Long.valueOf(value));
        }
    }),
    CREATED_DATE (dto -> dto.getCreatedDate() != null ?
     dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A", 
    (filter, value) -> filter.setCreatedDate(value));

    private final Function<EmployeeResponse, String> extractor;
    private final BiConsumer<UpdateEmployeeRequest, String> filterSetter;

    public String extract(EmployeeResponse dto) {
        return extractor.apply(dto);
    }

    public void applyFilter(UpdateEmployeeRequest filter, String value) {
        filterSetter.accept(filter, value);
    }
}
