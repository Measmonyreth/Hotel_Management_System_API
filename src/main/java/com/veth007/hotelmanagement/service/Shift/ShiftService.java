package com.veth007.hotelmanagement.service.Shift;

import java.util.List;
import java.util.Map;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Shift.ShiftRequest;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftListResponse;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftResponse;

public interface ShiftService {
    ApiResponse<ShiftResponse> createShift(ShiftRequest request);
    ApiResponse<List<ShiftListResponse>> getAllShift(int page, int size, boolean isPage);
    ApiResponse<ShiftResponse> getShiftById(Long id);
    ApiResponse<ShiftResponse> updateShift(Long id, ShiftRequest request);
    ApiResponse<String> deleteShift(Long id);
    ApiResponse<List<ShiftResponse>> list_deleted();
    ApiResponse<String> restoreShift(Long id);
    ApiResponse<ShiftListResponse> searchShifts(Map<String, String> allParams, String sort, int page, int size, boolean isPage);

}
