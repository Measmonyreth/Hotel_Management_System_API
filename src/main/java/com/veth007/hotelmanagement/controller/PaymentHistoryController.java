package com.veth007.hotelmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.service.BookingWithPayment.PaymentHistory.PaymentHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment-history")
@RequiredArgsConstructor
public class PaymentHistoryController {
    private final PaymentHistoryService paymentHistoryService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
            return ResponseEntity.ok(this.paymentHistoryService.get_all(page, size, isPage));
      
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> getById(@PathVariable String id) {
            return ResponseEntity.ok(this.paymentHistoryService.get_by_id(HashIdUtil.resolveId(id)));
      
    }
}
