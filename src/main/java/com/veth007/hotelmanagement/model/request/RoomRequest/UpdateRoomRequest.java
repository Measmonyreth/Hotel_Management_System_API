package com.veth007.hotelmanagement.model.request.RoomRequest;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest implements Serializable {
    private String roomName;
    private String rommNumber;
    private String description;
    private String statusRoom;
    private Double rating;
    private Double discountPercentage;
    private String action;
    private String roomType;
    private Double price;
    private String createDated;
}
