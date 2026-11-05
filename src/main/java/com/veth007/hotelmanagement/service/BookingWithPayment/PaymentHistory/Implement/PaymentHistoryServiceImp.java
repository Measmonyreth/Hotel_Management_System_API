package com.veth007.hotelmanagement.service.BookingWithPayment.PaymentHistory.Implement;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.PaymentHistory;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.repository.PaymentHistoryRepository;
import com.veth007.hotelmanagement.service.BookingWithPayment.PaymentHistory.PaymentHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImp implements PaymentHistoryService {
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(readOnly = true)
    public ApiResponse<List<PaymentHistoryResponse>> get_all(int page, int size, boolean isPage) {
        try {
            List<PaymentHistory> paymentHistory;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
                paymentHistory = this.paymentHistoryRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
                paymentHistory = this.paymentHistoryRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(paymentHistory.stream()
                    .map(PaymentHistoryResponse::fromEntity)
                    .toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve payments: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<PaymentHistoryResponse> get_by_id(Long id) {
        PaymentHistory paymentHistory = this.paymentHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
        try {

            return ApiResponse.success(PaymentHistoryResponse.fromEntity(paymentHistory));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve payment: " + e.getMessage(), e);
        }
    }
}
