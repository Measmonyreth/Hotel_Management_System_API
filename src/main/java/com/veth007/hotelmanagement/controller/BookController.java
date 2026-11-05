package com.veth007.hotelmanagement.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;
import com.veth007.hotelmanagement.model.request.Book.UpdateBookRequest;
import com.veth007.hotelmanagement.model.request.BookItems.UpdateBookItemsRequest;
import com.veth007.hotelmanagement.model.request.BookingWithPaymentRequest.BookingWithPaymentRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;
import com.veth007.hotelmanagement.model.response.Book.BookItemResponse;
import com.veth007.hotelmanagement.model.response.Book.BookListReponse;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;
import com.veth007.hotelmanagement.service.BookingWithPayment.Book.BookService;
import com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment.BookingWithPayment;

import lombok.RequiredArgsConstructor;

@RestController​
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookController {
    private final BookService service;
    private final BookingWithPayment bookingWithPayment;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PostMapping("/book/create")
    public ResponseEntity<ApiResponse<BookResponse>> create(@RequestBody BookingWithPaymentRequest request,
            @AuthenticationPrincipal EmployeeDetails userDetails) {
        return ResponseEntity.ok(this.bookingWithPayment.createBookingWithPayment(request));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/book/getall")
    public ResponseEntity<ApiResponse<BookListReponse>> getAll() {
        return ResponseEntity.ok(this.service.getAll());
    }

    @PreAuthorize("(isAuthenticated() and hasAuthority('CUSTOMER'))")
    @GetMapping("/book/get_by_customer")
    public ResponseEntity<ApiResponse<BookResponse>> getMethodName(
            @AuthenticationPrincipal CustomerDetails userDetails) {
        return ResponseEntity.ok(this.service.getByCustomer(userDetails.getId()));
    }

    @PreAuthorize("(isAuthenticated() and hasAuthority('CUSTOMER'))")
    @GetMapping("/book/get_history_book_customer")
    public ResponseEntity<ApiResponse<BookResponse>> get_history_book_cus(
            @AuthenticationPrincipal CustomerDetails userDetails) {
        return ResponseEntity.ok(this.service.get_history_book_cus(userDetails.getId()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @GetMapping({ "/book/getbyid", "/book/getbyid/{id}" })
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable(required = false) String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CUSTOMER"))) {
            // customer id comes from the auth principal, not the path
            return ResponseEntity.ok(this.service.get_byCustomer(userDetails.getUsername()));
        } else {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Booking id is required");
            }
            return ResponseEntity.ok(this.service.get_byId(HashIdUtil.resolveId(id)));
        }

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/book/deletebyid/{id}")
    public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.service.delete_byId(HashIdUtil.resolveId(id)));
        } catch (Exception e) {
            throw new RuntimeException("Error when delete booking by id: " + e.getMessage());
        }
    }

    // ─── Book item lifecycle: check-in → check-out → completed ───

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PatchMapping({ "/book/{id}/checkin", "/book/item/{id}/checkin" })
    public ResponseEntity<ApiResponse<BookResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.checkIn(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PatchMapping({ "/book/{id}/checkout", "/book/item/{id}/checkout" })
    public ResponseEntity<ApiResponse<BookResponse>> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.checkOut(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PatchMapping({ "/book/{id}/complete", "/book/item/{id}/complete" })
    public ResponseEntity<ApiResponse<BookResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.complete(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PatchMapping("/book/item/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelBookItem(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.cancel_book_item(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @PatchMapping("/book/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.cancel_booking(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/book/search")
    public ResponseEntity<ApiResponse<BookListReponse>> search(
            @RequestParam Map<String, String> allParams,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        UpdateBookRequest request = new UpdateBookRequest();
        // Initialize nested objects to avoid NullPointerException
        request.setUpdateCustomerRequest(
                new UpdateCustomerRequest());
        request.setUpdateBookItemsRequest(
                new UpdateBookItemsRequest());

        try {

            return ResponseEntity.ok(service.search_filter(allParams, request, sort, page, size, isPage, search));
        } catch (Exception e) {
            throw e;
        }
    }
}
