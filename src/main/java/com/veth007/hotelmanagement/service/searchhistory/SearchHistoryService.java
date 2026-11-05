package com.veth007.hotelmanagement.service.searchhistory;

import java.util.List;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.searchhistory.SearchHistoryRequest;
import com.veth007.hotelmanagement.model.response.searchhistory.SearchHistoryResponse;

public interface SearchHistoryService {
    ApiResponse<SearchHistoryResponse> record(SearchHistoryRequest request, Long customer_id);
    ApiResponse<List<SearchHistoryResponse>> history(Long customerId);
    ApiResponse<Long> count(Long customerId);
    ApiResponse<String> delete(Long id);
}
