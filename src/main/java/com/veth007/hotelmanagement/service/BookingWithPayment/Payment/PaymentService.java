package com.veth007.hotelmanagement.service.BookingWithPayment.Payment;

import java.math.BigDecimal;
import java.util.Map;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.request.Payment.PaymentRequest;
import com.veth007.hotelmanagement.model.request.Payment.RefundRequest;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentListResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentResponse;

public interface PaymentService {
    PaymentEntity processPayment(PaymentRequest request);
    ApiResponse<PaymentHistoryResponse> refund(Long paymentId, RefundRequest request);
    BigDecimal calculatePaypalAmount(Long bookId, String option);
    ApiResponse<PaymentResponse> payment_booking(PaymentRequest request);
    ApiResponse<PaymentListResponse> getIncompletePayments();
    ApiResponse<PaymentResponse> payment_booking_with_paypal(Long bookId, BigDecimal amount, String transactionId, String email);
    ApiResponse<PaymentListResponse> get_all(int page, int size, boolean isPage);
    ApiResponse<PaymentResponse> get_by_id(Long id);
    ApiResponse<String> delete(Long id);
    ApiResponse<String> restore(Long id);
    ApiResponse<PaymentListResponse> search_filter(Map<String, String> allParams, String sort, String search, int page, int size, boolean isPage);
}
