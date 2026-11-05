package com.veth007.hotelmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.request.Cancel.CancelRequest;
import com.veth007.hotelmanagement.model.response.Cancel.CancelResponse;
import com.veth007.hotelmanagement.service.Cancel.CancelService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/cancel-requests")
@RequiredArgsConstructor
public class CancelRequestController {

    private final CancelService cancelRequestService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CancelResponse>> submitCancelRequest(@RequestBody CancelRequest input) {
        return ResponseEntity.ok(cancelRequestService.createCancellationRequest(input.getBookId(), input.getReason()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CancelResponse>>> getAllCancelRequests() {
        return ResponseEntity.ok(cancelRequestService.getAllRequests());
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<CancelResponse>> getCancelRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(cancelRequestService.getRequestById(id));
    }

    @PreAuthorize("isAuthenticated() and hasAuthority('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<List<CancelResponse>>> getCustomerCancelRequests(
            @AuthenticationPrincipal CustomerDetails customerDetails) {
        return ResponseEntity.ok(
                cancelRequestService.getCustomerRequests(customerDetails.getId()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CancelResponse>> updateRequest(
            @PathVariable Long id,
            @RequestBody CancelRequest input) {
        return ResponseEntity.ok(cancelRequestService.updateRequest(id, input));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRequest(@PathVariable Long id) {
        return ResponseEntity.ok(cancelRequestService.deleteRequest(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PutMapping("/update-status/{id}")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @RequestBody CancelRequest input,
            @PathVariable Long id) {
        return ResponseEntity.ok(cancelRequestService.updateStatus(id, input));
    }
    
}
