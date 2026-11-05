package com.veth007.hotelmanagement.service.VipCard;

import java.util.Map;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.UpdateVipCustomerRequest;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.VipCustomerRequest;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipListCustomerResponse;

public interface VipCustomerService {
    ApiResponse<VipCustomerResponse> create(VipCustomerRequest request);
    ApiResponse<VipCustomerResponse> update(Long id, UpdateVipCustomerRequest request);
    ApiResponse<VipListCustomerResponse> get_all(int page, int size, boolean isPage);
    ApiResponse<VipCustomerResponse> get_by_id(Long id);
    ApiResponse<VipCustomerResponse> get_for_me(Long cus_id);
    ApiResponse<VipCustomerResponse> get_by_cusId(Long cus_id);
    ApiResponse<String> delete(Long id);
    ApiResponse<String> restore(Long id);
    ApiResponse<VipListCustomerResponse> search_filter(Map<String, String> allParams, String sort, String search, int page, int size, boolean isPage);
}
