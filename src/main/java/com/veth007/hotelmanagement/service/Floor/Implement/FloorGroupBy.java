package com.veth007.hotelmanagement.service.Floor.Implement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.veth007.hotelmanagement.model.request.FloorRequest.UpdateFloorRequest;
import com.veth007.hotelmanagement.model.response.Floor.FloorResponse;


@Getter
@RequiredArgsConstructor
public enum FloorGroupBy {
    STATUS_FLOOR(dto -> dto.getStatusFloor() != null ? dto.getStatusFloor() : "N/A",
            (filter, value) -> filter.setStatusFloor(value)),
    ACTION(dto -> dto.getAction() != null ? dto.getAction() : "N/A",
            (filter, value) -> filter.setAction(value)),
    CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreateDated(value));
    
    private final Function<FloorResponse, String> extractor;
    private final  BiConsumer<UpdateFloorRequest, String> filterSetter ;

    public void applyFilter(UpdateFloorRequest filter, String value) {
        filterSetter.accept(filter, value);
    }

    public String extract(FloorResponse dto) {
        return extractor.apply(dto);
    }
}
