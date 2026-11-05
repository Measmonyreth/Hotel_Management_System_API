package com.veth007.hotelmanagement.model.response.Floor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.FloorEntity;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorResponse implements Serializable {
    private Long id;
    private String floorName;
    private String floorNumber;
    private String description;
    private String statusFloor;
    private String action;
    private long totalRooms;
    private long availableRooms;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;
    private List<RoomResponse> rooms;

    public static FloorResponse mapToResponse(FloorEntity floor) {
        return mapToResponse(floor, 0, 0);
    }

    static public FloorResponse mapToResponse(FloorEntity floor, long totalRooms, long availableRooms) {
        return FloorResponse.builder()
                .id(floor.getId())
                .floorName(floor.getFloorName())
                .floorNumber(floor.getFloorNumber())
                .description(floor.getDescription())
                .statusFloor(floor.getStatusFloor().name())
                .action(floor.getAction().name())
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .createdDate(floor.getCreatedDate())
                .lastModifiedDate(floor.getLastModifiedDate())
                .deletedDate(floor.getDeletedDate())
                .createBy(floor.getCreatedBy())
                .lastModifiedBy(floor.getLastModifiedBy())
                .deletedBy(floor.getDeleted_by())
                .rooms(org.hibernate.Hibernate.isInitialized(floor.getRooms()) && floor.getRooms() != null
                        ? floor.getRooms().stream()
                                .map(RoomResponse::fromEntity)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
