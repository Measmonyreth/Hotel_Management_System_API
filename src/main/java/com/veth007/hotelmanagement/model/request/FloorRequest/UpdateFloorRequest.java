package com.veth007.hotelmanagement.model.request.FloorRequest;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFloorRequest implements Serializable {
    private String floorName;
    private String floorNumber;
    private String description;
    private String statusFloor;
    private String action;
    private String createDated;
}
