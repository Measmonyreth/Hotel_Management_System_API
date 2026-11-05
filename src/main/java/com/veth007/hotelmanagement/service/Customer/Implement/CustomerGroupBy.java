package com.veth007.hotelmanagement.service.Customer.Implement;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;
import com.veth007.hotelmanagement.model.response.Customer.CustomerResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerGroupBy {
    STATUS(dto -> dto.getStatus() != null ? dto.getStatus() : "N/A",
            (filter, value) -> filter.setStatus(value)),
    NATIONALITY(dto -> dto.getNationality() != null ? dto.getNationality() : "N/A",
            (filter, value) -> filter.setNationality(value)),
    COUNTRY(dto -> dto.getCountry() != null ? dto.getCountry() : "N/A",
            (filter, value) -> filter.setCountry(value)),
    CREATED_DATE(
            dto -> dto.getCreatedDate() != null ? dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : "N/A",
            (filter, value) -> filter.setCreateDated(value));

    private final Function<CustomerResponse, String> extractor;
    private final BiConsumer<UpdateCustomerRequest, String> filterSetter;

    public String extract(CustomerResponse dto) {
        return extractor.apply(dto);
    }

    public void applyFilter(UpdateCustomerRequest filter, String value) {
        filterSetter.accept(filter, value);
    }
}
