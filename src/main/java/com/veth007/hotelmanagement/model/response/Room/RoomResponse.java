package com.veth007.hotelmanagement.model.response.Room;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse implements Serializable {
    private Long id;
    private String roomName;
    private String roomNumber;
    private String description;
    private String action;
    private String statusRoom;
    private List<RoomImageResponse> images;
    private String roomType;
    private Double rating;
    private Double discountPercentage;
    private BigDecimal discountPrice;
    private BigDecimal price;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;
    private Map<String, Object> floor;

    static public RoomResponse fromEntity(RoomEntity roomEntity) {
        return RoomResponse.builder()

                .id(roomEntity.getId())
                .roomName(roomEntity.getRoomName())
                .roomNumber(roomEntity.getRoomNumber())
                .description(roomEntity.getDescription())
                .action(roomEntity.getAction().name())
                .statusRoom(roomEntity.getStatusRoom().name())
               .images(roomEntity.getImages() != null  // 👈 null guard
                ? roomEntity.getImages().stream()
                    .map(RoomImageResponse::fromEntity)
                    .collect(Collectors.toList())
                : new ArrayList<>())
                .roomType(roomEntity.getType().name())
                .discountPercentage(roomEntity.getDiscountPercentage())
                .discountPrice(roomEntity.getDiscountPrice())
                .rating(roomEntity.getRating())
                .price(roomEntity.getPrice())
                .createdDate(roomEntity.getCreatedDate())
                .lastModifiedDate(roomEntity.getLastModifiedDate())
                .deletedDate(roomEntity.getDeletedDate())
                .createBy(roomEntity.getCreatedBy())
                .lastModifiedBy(roomEntity.getLastModifiedBy())
                .deletedBy(roomEntity.getDeleted_by())
                .floor(roomEntity.getFloor() != null
                        ? Map.of("id", HashIdUtil.encodeId(roomEntity.getFloor().getId()),
                                "floorNumber", roomEntity.getFloor().getFloorNumber())
                        : null)
                .build();
    }
}
