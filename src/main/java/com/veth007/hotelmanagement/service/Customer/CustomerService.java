package com.veth007.hotelmanagement.service.Customer;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.CustomerRequest.CustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.LoginCustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;
import com.veth007.hotelmanagement.model.response.AuthCustomerResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerListResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;

public interface CustomerService {
    ApiResponse<CustomerResponse> createCustomer(CustomerRequest request);
    ApiResponse<ValidationResponse> isEmailAvailable(String email);
    ApiResponse<ValidationResponse> isPhoneAvailable(String phone);
    ApiResponse<ValidationResponse> validateVipCard(Long customerId);
    ApiResponse<ValidationResponse> validateStatus(Long customerId);

    ApiResponse<AuthCustomerResponse> login(LoginCustomerRequest request);

    ApiResponse<AuthCustomerResponse> refreshToken(String refreshToken, UserDetails userDetails);

    ApiResponse<AuthCustomerResponse.UserInfo> me(String email);

    ApiResponse<String> logout(HttpServletRequest request);

    ApiResponse<CustomerListResponse> getAllCustomers(int page, int size, boolean isPage);

    ApiResponse<CustomerResponse> updateCustomer(UpdateCustomerRequest request);

    ApiResponse<String> deleteCustomerById(Long customerId);

    ApiResponse<CustomerResponse> getByidWithVipcard(Long customerId);

    ApiResponse<String> restoreCustomerById(Long customerId);

    ApiResponse<CustomerListResponse> searchFilter(Map<String, String> allParams, UpdateCustomerRequest request, String sort, int page, int size, boolean isPage);

    ApiResponse<CustomerListResponse> searchFilter(Map<String, String> allParams, UpdateCustomerRequest request, String search, String sort, int page, int size, boolean isPage);

}
