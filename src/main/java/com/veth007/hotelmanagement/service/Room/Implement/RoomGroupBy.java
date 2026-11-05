package com.veth007.hotelmanagement.service.Room.Implement;

import com.veth007.hotelmanagement.model.request.RoomRequest.UpdateRoomRequest;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum RoomGroupBy {
    STATUS_ROOM (dto -> dto.getStatusRoom() != null ? dto.getStatusRoom() : "N/A",
            (filter, value) -> filter.setStatusRoom(value)),
    TYPE (dto -> dto.getRoomType() != null ? dto.getRoomType() : "N/A",
            (filter, value) -> filter.setRoomType(value)),
    ACTION (dto -> dto.getAction() != null ? dto.getAction() : "N/A",
            (filter, value) -> filter.setAction(value)),
        CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreateDated(value));

    private final Function<RoomResponse, String> extractor;
    private final BiConsumer<UpdateRoomRequest, String> filterSetter;

    public void applyFilter(UpdateRoomRequest filter, String value) {
        filterSetter.accept(filter, value);
    }

    public String extract(RoomResponse dto) {
        return extractor.apply(dto);
    }
}
