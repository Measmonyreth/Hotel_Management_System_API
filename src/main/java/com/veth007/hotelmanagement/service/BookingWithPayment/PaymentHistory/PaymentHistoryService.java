package com.veth007.hotelmanagement.service.BookingWithPayment.PaymentHistory;

import java.util.List;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;

public interface PaymentHistoryService {
    ApiResponse<List<PaymentHistoryResponse>> get_all(int page, int size, boolean isPage);
    ApiResponse<PaymentHistoryResponse>  get_by_id(Long id);
}
