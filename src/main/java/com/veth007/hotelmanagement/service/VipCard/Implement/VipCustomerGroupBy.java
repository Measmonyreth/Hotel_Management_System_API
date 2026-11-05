package com.veth007.hotelmanagement.service.VipCard.Implement;

import java.util.function.BiConsumer;
import java.util.function.Function;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.UpdateVipCustomerRequest;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VipCustomerGroupBy {
    TYPE_VIP (dto -> dto.getType_vip() != null ? dto.getType_vip() : "N/A",
            (filter, value) -> filter.setType_vip(value)),
    STATUS (dto -> dto.getStatus() != null ? dto.getStatus() : "N/A",
            (filter, value) -> filter.setStatus(value)),
    CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreatedDate(value));

    private final Function<VipCustomerResponse, String> groupBy;
    private final BiConsumer<UpdateVipCustomerRequest, String> filterSetter;

    public String extractor(VipCustomerResponse dto) {
        return groupBy.apply(dto);
    }

    public void applyFilter(UpdateVipCustomerRequest filter, String value) {
        filterSetter.accept(filter, value);
    }
}
