package com.veth007.hotelmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.request.searchhistory.SearchHistoryRequest;
import com.veth007.hotelmanagement.model.response.searchhistory.SearchHistoryResponse;
import com.veth007.hotelmanagement.service.searchhistory.SearchHistoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<SearchHistoryResponse>> record(
            @RequestBody SearchHistoryRequest request,
            @AuthenticationPrincipal CustomerDetails customerDetails)
            {
        Long customerId = customerDetails.getCustomer().getId();
        return ResponseEntity.ok(this.searchHistoryService.record(request, customerId));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<List<SearchHistoryResponse>>> history(
            @PathVariable String customerId) {
        return ResponseEntity.ok(this.searchHistoryService.history(HashIdUtil.resolveId(customerId)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/count/{customerId}")
    public ResponseEntity<ApiResponse<Long>> count(@PathVariable String customerId) {
        return ResponseEntity.ok(this.searchHistoryService.count(HashIdUtil.resolveId(customerId)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String id) {
        return ResponseEntity.ok(this.searchHistoryService.delete(HashIdUtil.resolveId(id)));
    }

    @PreAuthorize("isAuthenticated() and hasAuthority('CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<SearchHistoryResponse>>> myHistory(
            @AuthenticationPrincipal CustomerDetails customerDetails) {
        Long customerId = customerDetails.getCustomer().getId();
        return ResponseEntity.ok(this.searchHistoryService.history(customerId));
    }
}
