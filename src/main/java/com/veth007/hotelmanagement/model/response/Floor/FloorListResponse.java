package com.veth007.hotelmanagement.model.response.Floor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"totalfloors", "openfloors", "closefloors", "reservedfloors", "maintenancefloors", "floors"})
public class FloorListResponse {
    private int totalfloors;
    private int openfloors;     
    private int closefloors;
    private int reservedfloors;
    private int maintenancefloors;
    private List<FloorResponse> floors;


    static public FloorListResponse mapToResponse(List<FloorResponse> floorResponses, int totalfloors, int openfloors, int closefloors, int reservedfloors, int maintenancefloors) {
        return FloorListResponse.builder()
                .totalfloors(totalfloors)
                .openfloors(openfloors)
                .closefloors(closefloors)
                .reservedfloors(reservedfloors)
                .maintenancefloors(maintenancefloors)          
                .floors(floorResponses)
                .build();
    }
}
