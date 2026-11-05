package com.veth007.hotelmanagement.service.Room;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.RoomRequest.RoomRequest;
import com.veth007.hotelmanagement.model.request.RoomRequest.UpdateRoomRequest;
import com.veth007.hotelmanagement.model.response.Room.RoomListResponse;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;

public interface RoomService {
    ApiResponse<ValidationResponse> isRoomNameAvailable(String roomName);
    ApiResponse<ValidationResponse> isRoomNumberAvailable(String roomNumber);
    ApiResponse<ValidationResponse> isRoomAvailable(String roomNumber);
    ApiResponse<RoomResponse> createRoom(RoomRequest request, MultipartFile file);
    ApiResponse<RoomResponse> updateRoom(Long roomId, UpdateRoomRequest request, MultipartFile file);
    ApiResponse<RoomResponse> getRoomById(Long roomId);
    ApiResponse<RoomListResponse> getAllRooms(String sort, int page, int size, boolean isPage);
    ApiResponse<String> deleteRoomById(Long roomId);
    ApiResponse<String> restore(Long roomId);
    ApiResponse<List<RoomResponse>> getAllDeletedRooms(String sort);
    ApiResponse<RoomListResponse> search(Map<String, String> allParams, String sort, String search, int page, int size, boolean isPage);
}
