package com.veth007.hotelmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.response.DiscountResponse;
import com.veth007.hotelmanagement.service.Discount.DiscountService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/discount") 
public class DiscountController {
    
    private final DiscountService discountService;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER')")
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
            return ResponseEntity.ok(this.discountService.get_all(page, size, isPage));
       
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER')")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getById(@PathVariable String id) {
            return ResponseEntity.ok(this.discountService.get_by_id(HashIdUtil.resolveId(id)));
    }    
}
