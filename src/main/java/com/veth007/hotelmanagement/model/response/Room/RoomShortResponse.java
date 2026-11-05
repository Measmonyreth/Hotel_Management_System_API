package com.veth007.hotelmanagement.model.response.Room;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomShortResponse {
    private Long id;
    private String name;

    public static RoomShortResponse fromEntity(Object room) {
        if (room == null) return null;
        if (room instanceof RoomEntity) {
            RoomEntity r = (RoomEntity) room;
            return RoomShortResponse.builder()
                    .id(r.getId())
                    .name(r.getRoomName())
                    .build();
        } else if (room instanceof RoomResponse) {
            RoomResponse r = (RoomResponse) room;
            return RoomShortResponse.builder()
                    .id(r.getId())
                    .name(r.getRoomName())
                    .build();
        } else if (room instanceof RoomShortResponse) {
            return (RoomShortResponse) room;
        } else {
            throw new IllegalArgumentException("Unsupported room type: " + room.getClass().getName());
        }
    }
}
