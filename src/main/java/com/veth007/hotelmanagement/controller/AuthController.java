package com.veth007.hotelmanagement.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.veth007.hotelmanagement.config.TokenBlacklist;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.LoginRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.RegisterRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.UpdateEmployeeRequest;
import com.veth007.hotelmanagement.model.response.AuthResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeListResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;
import com.veth007.hotelmanagement.service.EmployeeDetailsService;
import com.veth007.hotelmanagement.service.Employee.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklist tokenBlacklist;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or   hasAuthority('MANAGER'))")
    @GetMapping("/validate/username")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateUsername(
            @RequestParam @NotBlank(message = "Username is required") String username) {
        return ResponseEntity.ok(authService.isUsernameAvailable(username));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or   hasAuthority('MANAGER'))")
    @GetMapping("/validate/phone")
    public ResponseEntity<ApiResponse<ValidationResponse>> validatePhone(
            @RequestParam @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers and be 7-15 digits") String phone) {
        return ResponseEntity.ok(authService.isPhoneAvailable(phone));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/status/{employeeId}")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateStatus(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(authService.validateStatus(employeeId));
    }

    // POST /api/auth/login — public
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or   hasAuthority('MANAGER'))")
    // POST /api/auth/register — public endpoint for creating new employees
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/getProfile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(authService.getProfile(userDetails));

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF')  or hasAuthority('MANAGER') )")
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid Authorization header", 400));
        }
        String token = authHeader.substring(7);
        if (tokenBlacklist.isBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token is already blacklisted", 401));
        }
        tokenBlacklist.blacklist(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<EmployeeListResponse>> getAllEmployees(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

        return ResponseEntity.ok(
                authService.getAllEmployees(page, size, isPage));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') "
            + "or hasAuthority('MANAGER') "
            + "or T(com.veth007.hotelmanagement.config.HashIdUtil).resolveId(#id) == principal.id)")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(this.authService.getEmployeeById(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get customer by id: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER') or T(com.veth007.hotelmanagement.config.HashIdUtil).resolveId(#id) == principal.id)")
    @RequestMapping(value = "/update/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @AuthenticationPrincipal EmployeeDetailsService employeeDetails,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @PathVariable Long id) {
        // if (employeeDetails == null) {
        // throw new BusinessException("Unauthorized: No authenticated user");
        // }
        return ResponseEntity.ok(this.authService.updateEmployee(request, id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @AuthenticationPrincipal EmployeeDetailsService employeeDetails,
            @PathVariable Long id) {
        // if (employeeDetails == null) {
        // throw new BusinessException("Unauthorized: No authenticated user");
        // }

        return ResponseEntity.ok(this.authService.deleteEmployee(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<String>> restore(@PathVariable Long id) {
        return ResponseEntity.ok(this.authService.restoreEmployee(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<EmployeeListResponse>> search(
            @RequestParam Map<String, String> allParams,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "id:asc") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        UpdateEmployeeRequest filter = new UpdateEmployeeRequest();
        try {
            return ResponseEntity
                    .ok(authService.searchEmployeesWithKeyword(allParams, filter, search, sort, page, size, isPage));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when search employee: " + e.getMessage());
        }
    }
}
