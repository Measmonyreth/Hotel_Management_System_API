package com.veth007.hotelmanagement.service.Floor;

import java.util.List;
import java.util.Map;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.FloorRequest.FloorRequest;
import com.veth007.hotelmanagement.model.request.FloorRequest.UpdateFloorRequest;
import com.veth007.hotelmanagement.model.response.Floor.FloorListResponse;
import com.veth007.hotelmanagement.model.response.Floor.FloorResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;

public interface FloorService {
    ApiResponse<ValidationResponse> isFloorNameAvailable(String floorName);
    ApiResponse<ValidationResponse> isFloorNumberAvailable(String floorNumber);
    ApiResponse<FloorResponse> createFloor(FloorRequest floorRequest);
    ApiResponse<FloorResponse> getFloorById(Long id);
    ApiResponse<FloorListResponse> getAllFloor(int page, int size, boolean isPage);
    ApiResponse<FloorResponse> update(Long id, UpdateFloorRequest floorRequest);
    ApiResponse<String> delete(Long id);
    ApiResponse<List<FloorResponse>> list_deleted();
    ApiResponse<String> restore(Long id);
    ApiResponse<FloorListResponse> search_floor(Map<String, String> allParams, String sort, String search, int page, int size, boolean isPage);
}
