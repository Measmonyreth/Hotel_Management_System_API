package com.veth007.hotelmanagement.controller;

import com.veth007.hotelmanagement.exception.ApiResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.UpdateVipCustomerRequest;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.VipCustomerRequest;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipListCustomerResponse;
import com.veth007.hotelmanagement.service.VipCard.VipCustomerService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/vipcustomer")
public class VipCustomerController {
        private final VipCustomerService service;

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PostMapping("/create")
        public ResponseEntity<ApiResponse<VipCustomerResponse>> create(@RequestBody VipCustomerRequest request) {
                return ResponseEntity.ok(this.service.create(request));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PutMapping("/update/{id}")
        public ResponseEntity<ApiResponse<VipCustomerResponse>> update(@PathVariable Long id,
                        @RequestBody UpdateVipCustomerRequest request) {
                return ResponseEntity.ok(this.service.update(id, request));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getall")
        public ResponseEntity<ApiResponse<VipListCustomerResponse>> getAll(
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "3") int size,
                        @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

                return ResponseEntity.ok(this.service.get_all(page, size, isPage));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getbyid/{id}")
        public ResponseEntity<ApiResponse<VipCustomerResponse>> get_by_id(@PathVariable Long id) {
                return ResponseEntity.ok(this.service.get_by_id(id));
        }

        @PreAuthorize("isAuthenticated() and  hasAuthority('CUSTOMER')")
        @GetMapping("/getforme")
        public ResponseEntity<ApiResponse<VipCustomerResponse>> get_for_me(
                        @AuthenticationPrincipal CustomerDetails customerDetails) {
                Long id = customerDetails.getId();
                return ResponseEntity.ok(this.service.get_for_me(id));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('CUSTOMER'))")
        @GetMapping("/getbycusid/{cus_id}")
        public ResponseEntity<ApiResponse<VipCustomerResponse>> get_by_cusId(@PathVariable Long cus_id) {
                return ResponseEntity.ok(
                                this.service.get_by_cusId(cus_id));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @DeleteMapping("/delete/{id}")
        public ResponseEntity<ApiResponse<String>> deleted(@PathVariable String id) {
                return ResponseEntity.ok(this.service.delete(HashIdUtil.resolveId(id)));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PostMapping("/restore/{id}")
        public ResponseEntity<ApiResponse<String>> restore(@PathVariable String id) {
                return ResponseEntity.ok(this.service.restore(HashIdUtil.resolveId(id)));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/search")
        public ResponseEntity<ApiResponse<VipListCustomerResponse>> search(
                        @RequestParam Map<String, String> allParams,
                        @RequestParam(required = false, defaultValue = "") String search,
                        @RequestParam(required = false, defaultValue = "id:asc") String sort,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "3") int size,
                        @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

                return ResponseEntity.ok(this.service.search_filter(allParams, sort, search, page, size, isPage));
        }
}
