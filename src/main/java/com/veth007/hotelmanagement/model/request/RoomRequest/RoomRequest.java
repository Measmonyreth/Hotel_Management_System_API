package com.veth007.hotelmanagement.model.request.RoomRequest;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest implements Serializable {

    private String roomName;
    @NotNull(message = "Room number is required")
    private String roomNumber;
    private Double rating;
    private Double discountPercentage;
    private String description;

    @NotNull(message = "Room type is required")
    private String roomType;
    private Double price;
    @NotNull(message = "Floor id is required")
    private Long floorId;
}
