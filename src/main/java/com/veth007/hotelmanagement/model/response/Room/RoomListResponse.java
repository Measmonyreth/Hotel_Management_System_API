package com.veth007.hotelmanagement.model.response.Room;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponse implements Serializable {
    private int totalRooms;
    private int availableRooms; // available
    private int bookedRooms; // unavailable
    private int occupiedRooms; // occupied
    private int maintenanceRooms; // under maintenance
    private int outOfServiceRooms; // out of service`
    private int pendingRooms; // pending
    private int cleaningRooms; // cleaning

    private int singleRooms; // single
    private int doubleRooms; // double
    private int suiteRooms; // suite
    private int twinRooms; // twin
    private int familyRooms; // family
    private int closeRooms; // close

    private int deletedRooms; // deleted
    private List<RoomResponse> rooms;

    // public static RoomListResponse mapToResponse(List<RoomResponse> roomResponses, int totalRooms, int availableRooms,
    //         int bookedRooms, int occupiedRooms, int maintenanceRooms, int closeRooms, int outOfServiceRooms,
    //         int pendingRooms, int singleRooms, int doubleRooms, int suiteRooms, int twinRooms, int familyRooms,
    //         int deletedRooms, int cleaningRooms) {
    //     return RoomListResponse.builder()
    //             .rooms(roomResponses)
    //             .totalRooms(totalRooms)
    //             .availableRooms(availableRooms)
    //             .bookedRooms(bookedRooms)
    //             .occupiedRooms(occupiedRooms)
    //             .maintenanceRooms(maintenanceRooms)
    //             .closeRooms(closeRooms)
    //             .outOfServiceRooms(outOfServiceRooms)
    //             .pendingRooms(pendingRooms)
    //             .singleRooms(singleRooms)
    //             .doubleRooms(doubleRooms)
    //             .suiteRooms(suiteRooms)
    //             .twinRooms(twinRooms)
    //             .familyRooms(familyRooms)
    //             .deletedRooms(deletedRooms)
    //             .cleaningRooms(cleaningRooms)
    //             .build();
    // }
}
