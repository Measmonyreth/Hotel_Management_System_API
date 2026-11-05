package com.veth007.hotelmanagement.service.Discount;

import java.util.List;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.response.DiscountResponse;

public interface DiscountService {
    ApiResponse<List<DiscountResponse>> get_all(int page, int size, boolean isPage);
    ApiResponse<DiscountResponse> get_by_id(Long id);
}
