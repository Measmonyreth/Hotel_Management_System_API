package com.veth007.hotelmanagement.service.Cancel;

import java.util.List;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Cancel.CancelRequest;
import com.veth007.hotelmanagement.model.response.Cancel.CancelResponse;

public interface CancelService {
    ApiResponse<CancelResponse> createCancellationRequest(Long bookId, String reason);
    ApiResponse<List<CancelResponse>> getAllRequests();
    ApiResponse<CancelResponse> getRequestById(Long id);
    ApiResponse<List<CancelResponse>> getCustomerRequests(Long customerId);
    ApiResponse<CancelResponse> updateRequest(Long id, CancelRequest request);
    ApiResponse<String> deleteRequest(Long id);
    ApiResponse<String> updateStatus(Long id, CancelRequest request);

}
