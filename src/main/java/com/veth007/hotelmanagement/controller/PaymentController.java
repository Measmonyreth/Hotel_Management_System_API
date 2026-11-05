package com.veth007.hotelmanagement.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Payment.PaymentRequest;
import com.veth007.hotelmanagement.model.request.Payment.RefundRequest;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentListResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentResponse;
import com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment.BookingWithPayment;
import com.veth007.hotelmanagement.service.BookingWithPayment.Payment.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final BookingWithPayment bookingWithPayment;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponse>> create(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(this.paymentService.payment_booking(request));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<PaymentListResponse>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        return ResponseEntity.ok(this.paymentService.get_all(page, size, isPage));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(this.paymentService.get_by_id(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/refund/{id}")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> refund(
            @PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(id, request));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.paymentService.delete(HashIdUtil.resolveId(id)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete payment: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<String>> restore(@PathVariable String id) {
        return ResponseEntity.ok(this.paymentService.restore(HashIdUtil.resolveId(id)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/search_filter")
    public ResponseEntity<ApiResponse<PaymentListResponse>> search(
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

        try {
            return ResponseEntity.ok(this.paymentService.search_filter(allParams, sort, search, page, size, isPage));
        } catch (Exception e) {
            throw e;
        }
    }

    // incomplete payments (unpaid / deposit only): shown on the
    // /paypal/unpaid page so the remaining balance can be paid
    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/incomplete")
    public ResponseEntity<ApiResponse<PaymentListResponse>> incompletePayments() {
        return ResponseEntity.ok(this.paymentService.getIncompletePayments());
    }
    // Two modes:
    // 1. bookId given → pay an existing booking (e.g. remaining balance)
    // 2. booking details given → pay-first flow: nothing is saved yet,
    // the booking is created in /success only after PayPal approved
 //   @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER'))")
    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> createPayment(
            @RequestParam(required = false) String bookId,
            @RequestParam(defaultValue = "FULL") String option,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) String recipientToken,
            @RequestParam(required = false, defaultValue = "ONLINE") String bookingType) {

        String base = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        Map<String, String> result = bookingWithPayment.initiatePayment(
                bookId,
                option,
                roomId,
                customerId,
                employeeId,
                checkIn,
                checkOut,
                bookingType,
                recipientToken,
                base);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

  //  @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER'))")
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Object>> paymentSuccess(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false, defaultValue = "ONLINE") String bookingType,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam(required = false) String recipientToken) {

        return ResponseEntity.ok(bookingWithPayment.handlePaymentSuccess(
                bookId, roomId, customerId, employeeId, checkIn, checkOut, bookingType, paymentId, payerId, recipientToken));
    }

}
