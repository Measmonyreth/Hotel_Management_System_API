package com.veth007.hotelmanagement.controller;
import com.veth007.hotelmanagement.config.TokenBlacklist;
import com.veth007.hotelmanagement.exception.ApiResponse;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.veth007.hotelmanagement.model.request.CustomerRequest.CustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.LoginCustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;
import com.veth007.hotelmanagement.model.request.Google.GoogleTokenRequest;
import com.veth007.hotelmanagement.model.response.AuthCustomerResponse;
import com.veth007.hotelmanagement.model.response.AuthResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerListResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.service.Customer.CustomerService;
import com.veth007.hotelmanagement.service.OAuth2.OAuth2UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {
    private final TokenBlacklist tokenBlacklist;
    private final CustomerService customerService;
    private final OAuth2UserService oAuth2UserService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('STAFF') or hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/vip/{customerId}")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateVipCard(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.validateVipCard(customerId));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('STAFF') or hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/status/{customerId}")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateStatus(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.validateStatus(customerId));
    }

     @PreAuthorize("isAuthenticated() and (hasAuthority('STAFF') or hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/email")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateEmail(
            @RequestParam @Email(message = "Invalid email address") String email) {
        return ResponseEntity.ok(customerService.isEmailAvailable(email.trim().toLowerCase()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('STAFF') or hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/phone")
    public ResponseEntity<ApiResponse<ValidationResponse>> validatePhone(
            @RequestParam
            @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers and be 7-15 digits")
            String phone) {
        return ResponseEntity.ok(customerService.isPhoneAvailable(phone));
    }

//    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER'))")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    
    @PostMapping("/login/google/mobile")
    public ResponseEntity<ApiResponse<AuthResponse>> googleMobileLogin(
            @Valid @RequestBody GoogleTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(oAuth2UserService.loginWithGoogleToken(request.getIdToken())));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthCustomerResponse>> login(@Valid @RequestBody LoginCustomerRequest request) {

            return ResponseEntity.ok( this.customerService.login(request));
    
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('CUSTOMER'))")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthCustomerResponse>> refresh(@RequestParam String refreshToken,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(this.customerService.refreshToken(refreshToken, userDetails));
    }

    /** Get current logged-in user info (protected) */
    @PreAuthorize("isAuthenticated() and (hasAuthority('CUSTOMER'))")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthCustomerResponse.UserInfo>> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(customerService.me(userDetails.getUsername()));
    }

    // @GetMapping("/developer/customers/profile")
    // public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomerEntity
    // customer) {
    // try {
    // return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
    // } catch (Exception e) {
    // Map<String, Object> error = new HashMap<>();
    // error.put("message", e.getMessage());
    // return ResponseEntity.status(500).body(error);
    // }
    // }

    @PreAuthorize("isAuthenticated() and hasAuthority('CUSTOMER')")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        if (tokenBlacklist.isBlacklisted(token)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is already blacklisted"));
        }
        tokenBlacklist.blacklist(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

     @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<CustomerListResponse>> getAllCustomer(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

        return ResponseEntity.ok(this.customerService.getAllCustomers(page, size, isPage));

    }

    // List All By Status
    // @GetMapping("/auth/customers/getallbystatus")
    // public ResponseEntity<List<CustomerResponse>>
    // getAllCustomerByStatus(@RequestParam String statuss) {
    // try {
    // statuss = statuss.toUpperCase();
    // Status status = Status.valueOf(statuss);

    // List<CustomerEntity> customerEntity =
    // this.customerService.get_all_by_status(status);
    // return
    // ResponseEntity.ok(customerEntity.stream().map(CustomerResponse::fromEntity).toList());
    // } catch (Exception e) {
    // throw new RuntimeException("Failed to get all customer by status: " +
    // e.getMessage(), e);
    // }
    // }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deletedCustomer(@PathVariable Long id) {

        return ResponseEntity.ok(this.customerService.deleteCustomerById(id));

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('CUSTOMER') or hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(this.customerService.updateCustomer(request));

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/getbyid/withvipcard/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {

        return ResponseEntity.ok(this.customerService.getByidWithVipcard(id));

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<String>> restoreCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(this.customerService.restoreCustomerById(id));

    }

    // @GetMapping("/auth/customers/search")
    // public ResponseEntity<List<CustomerResponse>> search(
    // @RequestParam String name,
    // @RequestParam String sort) {
    // try {
    // List<CustomerEntity> customerEntity = this.customerService.search(name,
    // sort);

    // return
    // ResponseEntity.ok(customerEntity.stream().map(CustomerResponse::fromEntity).toList());
    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Failed to search customer: " + e.getMessage(),
    // e);
    // }
    // }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF')  or hasAuthority('MANAGER'))")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<CustomerListResponse>> list(@RequestParam Map<String, String> allParams,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        UpdateCustomerRequest filter = new UpdateCustomerRequest();
      
        // If search parameter is provided, use search_filter; otherwise use list_filter
        // with grouping
            return ResponseEntity.ok(customerService.searchFilter(allParams, filter, search, sort, page, size, isPage));
    }

}
