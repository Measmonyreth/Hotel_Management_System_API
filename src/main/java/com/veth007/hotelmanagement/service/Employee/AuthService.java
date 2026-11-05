package com.veth007.hotelmanagement.service.Employee;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.LoginRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.RegisterRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.UpdateEmployeeRequest;
import com.veth007.hotelmanagement.model.response.AuthResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeListResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;

public interface AuthService {
    ApiResponse<AuthResponse> login(LoginRequest request);
    ApiResponse<String> register(RegisterRequest request);
    ApiResponse<ValidationResponse> isUsernameAvailable(String username);
    ApiResponse<ValidationResponse> isPhoneAvailable(String phone);
    ApiResponse<ValidationResponse> validateStatus(Long employeeId);
    ApiResponse<EmployeeResponse> getProfile(UserDetails userDetails);
    ApiResponse<EmployeeListResponse> getAllEmployees(int page, int size, boolean isPage);
    ApiResponse<EmployeeResponse> getEmployeeById(Long employeeId);
    ApiResponse<EmployeeResponse> updateEmployee(UpdateEmployeeRequest request, Long employeeId);
    ApiResponse<EmployeeListResponse> searchEmployeesWithKeyword(Map<String, String> allParams, UpdateEmployeeRequest filter, String search, String sort, int page, int size, boolean isPage);
//    ApiResponse<EmployeeListResponse> searchEmployees(Map<String, String> allParams, UpdateEmployeeRequest filter, String sort);
    ApiResponse<String> deleteEmployee(Long employeeId);
    ApiResponse<String> restoreEmployee(Long employeeId);
    // ApiResponse<EmployeeListResponse>list_filter(UpdateEmployeeRequest request, EmployeeGroupBy f1, EmployeeGroupBy f2,
    //         EmployeeGroupBy f3, String search, String sort);
}
