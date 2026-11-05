package com.veth007.hotelmanagement.service.Customer.Implement;

import com.veth007.hotelmanagement.config.TokenBlacklist;
import com.veth007.hotelmanagement.config.Validation.GmailDomainValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.config.JwtUtil;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.VipStatus;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;
import com.veth007.hotelmanagement.model.entity.CustomerEntity.AuthProvider;
import com.veth007.hotelmanagement.model.request.CustomerRequest.CustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.LoginCustomerRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;
import com.veth007.hotelmanagement.model.response.AuthCustomerResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerListResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.VipCustomerRepository;
import com.veth007.hotelmanagement.service.Customer.CustomerService;
import com.veth007.hotelmanagement.service.EmailVerifire.Implement.EmailVerificationServiceImp;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImp implements CustomerService {
    private final TokenBlacklist tokenBlacklist;
    private final CustomerRepository customerRepository;
    private final VipCustomerRepository vipCustomerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private final GmailDomainValidator gmailDomainValidator;
    private final EmailVerificationServiceImp emailVerificationService;

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> createCustomer(CustomerRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email != null && email.isBlank()) {
            email = null;
        }
        String password = request.getPassword();
        if (password != null && password.isBlank()) {
            password = null;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isEmployee = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.ADMIN.name().equals(authority.getAuthority())
                        || Role.STAFF.name().equals(authority.getAuthority())
                        || Role.MANAGER.name().equals(authority.getAuthority()));

        if (!isEmployee && (email == null || password == null)) {
            throw new IllegalArgumentException("Email and password are required for customer registration.");
        }

        if (email != null) {
            if (!gmailDomainValidator.isGoogleEmail(email)) {
                throw new IllegalArgumentException("Invalid email domain: " + email);
            }

            if (customerRepository.existsByEmail(email)) {
                throw new AlreadyExistException("Email already exists: " + email);
            }
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new AlreadyExistException("Phone already exists: " + request.getPhone());
        }

        Optional<CustomerEntity> theLastCustomerId = customerRepository.findTopByOrderByCustomerIdDesc();
        Long lastId = 0L;

        if (theLastCustomerId.isPresent()) {
            String customerIdStr = theLastCustomerId.get().getCustomerId();
            if (customerIdStr != null && customerIdStr.contains("-")) {
                try {
                    lastId = Long.parseLong(customerIdStr.substring(customerIdStr.lastIndexOf("-") + 1));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid customer ID format: " + customerIdStr, e);
                }
            }
        }

        String newCustomerId = "CUS-" + String.format("%08d", lastId + 1);

        CustomerEntity customerEntity = CustomerEntity.builder()
                .customerId(newCustomerId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(password != null ? passwordEncoder.encode(password) : null)
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .provider(CustomerEntity.AuthProvider.LOCAL)
                .emailVerified(false)
                .nationality(request.getNationality())
                .city_or_province(request.getCity_or_province())
                .country(request.getCountry())
                .build();

        try {
            CustomerEntity savedCustomer = customerRepository.save(customerEntity);

            if (email != null) {
                try {
                    emailVerificationService.sendVerification(savedCustomer.getEmail());
                } catch (Exception emailException) {
                    // Log email error but don't fail registration
                    System.err.println("Warning: Failed to send verification email to " + savedCustomer.getEmail()
                            + ": " + emailException.getMessage());
                    // Optionally: Mark customer as needing email retry
                    // savedCustomer.setEmailVerificationSent(false);
                    // customerRepository.save(savedCustomer);
                    throw new RuntimeException("Failed to send verification email: " + emailException.getMessage(),
                            emailException);
                }
            }

            return ApiResponse.ok("Customer created successfully", CustomerResponse.fromEntity(savedCustomer));
        } catch (Exception e) {
            throw new RuntimeException("failed to create customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ValidationResponse> isEmailAvailable(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return customerRepository.findByEmail(normalizedEmail)
                .map(customer -> ApiResponse.ok(
                        "Email belongs to an " + customer.getStatus().name().toLowerCase() + " customer",
                        new ValidationResponse(false, customer.getStatus().name(), normalizedEmail)))
                .orElseGet(() -> ApiResponse.ok(
                        "Email is available",
                        new ValidationResponse(true, "AVAILABLE", normalizedEmail)));
    }

    @Override
    public ApiResponse<ValidationResponse> isPhoneAvailable(String phone) {
        return customerRepository.findByPhone(phone)
                .map(customer -> ApiResponse.ok(
                        "Phone belongs to an " + customer.getStatus().name().toLowerCase() + " customer",
                        new ValidationResponse(false, customer.getStatus().name(), phone)))
                .orElseGet(() -> ApiResponse.ok(
                        "Phone is available",
                        new ValidationResponse(true, "AVAILABLE", phone)));
    }

    @Override
    public ApiResponse<ValidationResponse> validateVipCard(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));

        return vipCustomerRepository.findByCustomerId(customerId)
                .map(vip -> ApiResponse.ok(
                        "Customer has a VIP card",
                        new ValidationResponse(false, vip.getStatus().name(), "VIP_CARD_EXISTS")))
                .orElseGet(() -> ApiResponse.ok(
                        "Customer does not have a VIP card",
                        new ValidationResponse(true, "NO_VIP_CARD", "VIP_CARD_DOES_NOT_EXIST")));
    }

    @Override
    public ApiResponse<ValidationResponse> validateStatus(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));

        Status status = customer.getStatus();
        boolean active = status == Status.ACTIVE;
        String message = active
                ? "Customer is active"
                : "Customer is " + status.name().toLowerCase();

        return ApiResponse.ok(message,
                new ValidationResponse(active, status.name(), customerId.toString()));
    }

    @Override
    @Transactional
    public ApiResponse<AuthCustomerResponse> login(LoginCustomerRequest request) {
        String email = request.email().trim().toLowerCase();
        // Must be Gmail
        if (!gmailDomainValidator.isGoogleEmail(email)) {
            throw new IllegalArgumentException(gmailDomainValidator.getRejectionReason(email));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));

            CustomerEntity user = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isEmailVerified()) {
                throw new IllegalStateException("Please verify your Gmail address before logging in.");
            }

            String accessToken = jwtUtil.generateAccessToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            return ApiResponse.ok("Login successful",
                    new AuthCustomerResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name()));

        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid email or password.");
        } catch (DisabledException e) {
            throw new IllegalStateException("Account is disabled.");
        }
    }

    // ─── Refresh token ─────────────────────────────────────
    @Override
    public ApiResponse<AuthCustomerResponse> refreshToken(String refreshToken, UserDetails userDetails) {
        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        CustomerEntity user = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessTokenFromEmail(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        return ApiResponse.ok("Token refreshed successfully",
                new AuthCustomerResponse(newAccessToken, newRefreshToken, email, user.getRole().name()));
    }

    // ─── Current user info ─────────────────────────────────
    @Override
    @Transactional
    public ApiResponse<AuthCustomerResponse.UserInfo> me(String email) {
        CustomerEntity user = customerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + email));

        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "")
                + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();

        return ApiResponse.ok("OK", AuthCustomerResponse.UserInfo.builder()
                .id(HashIdUtil.encodeId(user.getId()))
                .email(user.getEmail())
                .name(fullName)
                .phone(user.getPhone())
                .role(user.getRole().name())
                .picture(user.getPicture())
                .emailVerified(user.isEmailVerified())
                .provider(user.getProvider().name())
                .status(user.getStatus().name())
                .nationality(user.getNationality())
                .city_or_province(user.getCity_or_province())
                .country(user.getCountry())
                .vipCustomer(
                        user.getVipCustomer() != null ? VipCustomerResponse.fromEntity(user.getVipCustomer()) : null)
                .createdAt(user.getCreatedDate())
                .updatedAt(user.getLastModifiedDate())
                .build());
    }

    @Override
    // don't need implement
    public ApiResponse<String> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenBlacklist.blacklist(token);
            }
            return ApiResponse.ok("Logout successful");
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error during logout: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<CustomerListResponse> getAllCustomers(int page, int size, boolean isPage) {
        try {
            List<CustomerEntity> customerEntity;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
                customerEntity = this.customerRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
                customerEntity = this.customerRepository.findAll(pageable).getContent();
            }
            return ApiResponse.ok("OK", toCustomerListResponse(customerEntity));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(UpdateCustomerRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        System.out.println("=== UPDATE DEBUG ===");
        System.out.println("Principal type: " + principal.getClass().getName());
        System.out.println("Principal: " + principal);
        System.out.println("Authorities: " + auth.getAuthorities());
        System.out.println("isEmployee: " + (principal instanceof EmployeeDetails));
        System.out.println("isCustomer: " + (principal instanceof CustomerDetails));
        System.out.println("====================");

        boolean isEmployee = principal instanceof EmployeeDetails;
        boolean isCustomer = principal instanceof CustomerDetails;


        if (isEmployee) {
            // ADMIN flow
            if (request.getId() == null)
                throw new IllegalArgumentException("Customer ID is required for update");

            CustomerEntity customerEntity = this.findOne(request.getId());
            if (request.getFirstName() != null)
                customerEntity.setFirstName(request.getFirstName());
            if (request.getLastName() != null)
                customerEntity.setLastName(request.getLastName());
            if (request.getPhone() != null)
                customerEntity.setPhone(request.getPhone());
            if (request.getStatus() != null)
                customerEntity.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
            if (request.getRole() != null)
                customerEntity.setRole(Role.valueOf(request.getRole().toUpperCase()));
            if (request.getNationality() != null)
                customerEntity.setNationality(request.getNationality());
            if (request.getPicture() != null)
                customerEntity.setPicture(request.getPicture());

            if (request.getCountry() != null)
                customerEntity.setCountry(request.getCountry());
            if (request.getCity_or_province() != null)
                customerEntity.setCity_or_province(request.getCity_or_province());

            CustomerEntity saved = this.customerRepository.save(customerEntity);
            // Initialize lazy collections within the transaction
            Hibernate.initialize(saved.getCustomer_notification());
            Hibernate.initialize(saved.getFavoritesEntities());
            return ApiResponse.ok("Customer updated successfully", CustomerResponse.fromEntity(saved, false));

        } else if (isCustomer) {
            // CUSTOMER flow — use their own ID from principal
            CustomerDetails customerDetails = (CustomerDetails) principal;
            Long id = customerDetails.getId(); // ✅ from token, no DB lookup

            CustomerEntity customerEntity = this.findOne(id);
            if (request.getFirstName() != null)
                customerEntity.setFirstName(request.getFirstName());
            if (request.getLastName() != null)
                customerEntity.setLastName(request.getLastName());
            if (request.getPhone() != null)
                customerEntity.setPhone(request.getPhone());
            if (request.getNationality() != null)
                customerEntity.setNationality(request.getNationality());
            if (request.getCountry() != null)
                customerEntity.setCountry(request.getCountry());
            if (request.getPicture() != null)
                customerEntity.setPicture(request.getPicture());
            if (request.getCity_or_province() != null)
                customerEntity.setCity_or_province(request.getCity_or_province());

            CustomerEntity saved = this.customerRepository.save(customerEntity);
            // Initialize lazy collections within the transaction
            // Hibernate.initialize(saved.getFavoritesEntities());
                        Hibernate.initialize(saved.getCustomer_notification());
            return ApiResponse.ok("Customer updated successfully", CustomerResponse.fromEntity(saved, false));

        } else {
            throw new RuntimeException("Unauthorized to update customer");
        }
    }

    // list all customer by status
    // public List<CustomerEntity> get_all_by_status(Status status) {
    // try {
    // // uppercast
    // // status = Status.valueOf(status.toString().toUpperCase());

    // if (!this.customerRepository.existsByStatus(status)) {
    // throw new RuntimeException("No customer found with status: " + status);
    // }

    // List<CustomerEntity> customerEntity =
    // this.customerRepository.findByStatus(status);
    // return customerEntity.stream().toList();
    // } catch (Exception e) {
    // throw new RuntimeException("Failed to get all customer by status: " +
    // e.getMessage(), e);
    // }
    // }

    public CustomerEntity findOne(Long id) throws NotFoundException {
        if (customerRepository.existsByIdAndDeletedDateIsNotNull(id)
                && customerRepository.existsByIdAndStatus(id, Status.DELETED)) {
            throw new NotFoundException("Customer is already deleted with id: " + id);
        }
        CustomerEntity customerEntity = this.customerRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Customer not found with id: " + id));
        try {
            return customerEntity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to find customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> deleteCustomerById(Long id) {
        CustomerEntity customerEntity = this.findOne(id);
        try {

            String userName = SecurityContextHolder.getContext().getAuthentication().getName();

            customerEntity.setDeletedDate(LocalDateTime.now());
            customerEntity.setDeletedBy(userName);
            customerEntity.setStatus(Status.DELETED);
            if (customerEntity.getVipCustomer() != null) {
                customerEntity.getVipCustomer().setStatus(VipStatus.DELETED);
                customerEntity.getVipCustomer().setDeletedDate(LocalDateTime.now());
                customerEntity.getVipCustomer().setDeletedBy(userName);
            }
            this.customerRepository.save(customerEntity);

            // validate vip card if exist

            return ApiResponse.ok("Customer deleted successfully");

        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to delete customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> getByidWithVipcard(Long id) {
        CustomerEntity customerEntity = this.customerRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Customer not found with id: " + id));
        try {

            return ApiResponse.ok("Customer found with VIP card", CustomerResponse.fromEntity(customerEntity, true));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get customer by id with vip card: " + ex.getMessage(), ex);
        }
    }

    private CustomerEntity find_restore(Long id) {
        if (!this.customerRepository.existsByIdAndDeletedDateIsNotNull(id)
                && !this.customerRepository.existsByIdAndStatus(id, Status.DELETED)) {
            throw new NotFoundException("Customer is not deleted");
        }
        CustomerEntity customerEntity = this.customerRepository.findByIdAndDeletedDateIsNotNull(id).orElseThrow(
                () -> new NotFoundException("Customer not found with id: " + id));
        try {
            return customerEntity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to find customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> restoreCustomerById(Long id) {
        CustomerEntity customerEntity = this.find_restore(id);
        try {

            customerEntity.setDeletedDate(null);
            customerEntity.setDeletedBy(null);
            customerEntity.setStatus(Status.ACTIVE);

            if (customerEntity.getVipCustomer() != null) {
                customerEntity.getVipCustomer().setStatus(VipStatus.ACTIVE);
                customerEntity.getVipCustomer().setDeletedDate(null);
                customerEntity.getVipCustomer().setDeletedBy(null);
            }

            this.customerRepository.save(customerEntity);
            return ApiResponse.ok("Customer restored successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to restore customer: " + e.getMessage(), e);
        }
    }

    // public List<CustomerEntity> search(String name, String sort) {
    // try {

    // List<Sort.Order> sorts = new ArrayList<>();
    // for (String s : sort.split(",")) {
    // String[] str = s.split(":");

    // if (str.length != 2) {
    // throw new RuntimeException("Invalid sort format like: id:desc");
    // }

    // String direction = str[1].toUpperCase();
    // String field = str[0];

    // sorts.add(new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC :
    // Sort.Direction.DESC, field));
    // }

    // Map<String, String> search = new HashMap<>();
    // search.put("firstName", name);
    // search.put("lastName", name);
    // search.put("email", name);
    // search.put("phone", name);

    // return customerRepository.findAll((Specification<CustomerEntity>) (root,
    // query, criteriaBuilder) -> {
    // List<Predicate> predicates = new ArrayList<>();
    // for (Map.Entry<String, String> entry : search.entrySet()) {
    // String key = entry.getKey();
    // String value = entry.getValue();
    // if (value != null && !value.isEmpty()) {
    // predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(key).as(String.class)),
    // "%" + value.toLowerCase() + "%"));
    // }
    // }
    // return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    // }, Sort.by(sorts));

    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Failed to search customer: " + e.getMessage(),
    // e);
    // }
    // }
    @Override
    public ApiResponse<CustomerListResponse> searchFilter(Map<String, String> allParams, UpdateCustomerRequest filter,
            String sort, int page, int size, boolean isPage) {
        try {
            return searchFilter(allParams, filter, null, sort, page, size, isPage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<CustomerListResponse> searchFilter(Map<String, String> allParams, UpdateCustomerRequest filter,
            String search, String sort, int page, int size, boolean isPage) {
        try {
            List<CustomerGroupBy> groupBys = new ArrayList<>();

            allParams.forEach((key, value) -> {
                try {
                    CustomerGroupBy groupBy = CustomerGroupBy.valueOf(key.toUpperCase());
                    groupBys.add(groupBy);
                    groupBy.applyFilter(filter, value);
                } catch (IllegalArgumentException e) {
                    try {
                        switch (key.toLowerCase()) {
                            case "firstname" -> filter.setFirstName(value);
                            case "lastname" -> filter.setLastName(value);
                            case "email" -> filter.setEmail(value);
                            case "phone" -> filter.setPhone(value);
                        }
                    } catch (Exception ex) {
                        // TODO: handle exception
                        throw new RuntimeException("Invalid filter parameter: " + key);
                    }
                }
            });

            List<Sort.Order> sorts = new ArrayList<>();
            for (String s : sort.split(",")) {
                String[] str = s.split(":");

                if (str.length != 2) {
                    throw new RuntimeException("Invalid sort format like: id:desc");
                }

                String direction = str[1].toUpperCase();
                String field = str[0];

                sorts.add(new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));
            }
            Specification<CustomerEntity> spec = (root, query, cb) -> {
                if (search == null || search.isEmpty()) {
                    // Filter mode: use UpdateCustomerRequest fields
                    List<Predicate> predicates = new ArrayList<>();
                    if (filter.getFirstName() != null)
                        predicates.add(cb.like(cb.lower(root.get("firstName")),
                                "%" + filter.getFirstName().toLowerCase() + "%"));
                    if (filter.getLastName() != null)
                        predicates.add(cb.like(cb.lower(root.get("lastName")),
                                "%" + filter.getLastName().toLowerCase() + "%"));
                    if (filter.getEmail() != null)
                        predicates.add(cb.like(cb.lower(root.get("email")),
                                "%" + filter.getEmail().toLowerCase() + "%"));
                    if (filter.getPhone() != null)
                        predicates.add(cb.like(cb.lower(root.get("phone")),
                                "%" + filter.getPhone().toLowerCase() + "%"));
                    if (filter.getStatus() != null)
                        predicates.add(cb.equal(root.get("status"),
                                Status.valueOf(filter.getStatus().toUpperCase())));
                    if (filter.getNationality() != null)
                        predicates.add(cb.like(cb.lower(root.get("nationality")),
                                "%" + filter.getNationality().toLowerCase() + "%"));
                    if (filter.getCountry() != null)
                        predicates.add(cb.like(cb.lower(root.get("country")),
                                "%" + filter.getCountry().toLowerCase() + "%"));
                    if (filter.getCreateDated() != null && !filter.getCreateDated().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreateDated().trim(), dateTimeFormatter);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        predicates.add(cb.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }
                    return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
                } else {
                    // Search mode: search across multiple fields
                    List<Predicate> predicates = new ArrayList<>();
                    String searchTerm = "%" + search.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("firstName")), searchTerm));
                    predicates.add(cb.like(cb.lower(root.get("lastName")), searchTerm));
                    predicates.add(cb.like(cb.lower(root.get("email")), searchTerm));
                    predicates.add(cb.like(cb.lower(root.get("phone")), searchTerm));
                    if (filter.getStatus() != null)
                        predicates.add(cb.equal(root.get("status"),
                                Status.valueOf(filter.getStatus().toUpperCase())));
                    if (filter.getNationality() != null)
                        predicates.add(cb.like(cb.lower(root.get("nationality")),
                                "%" + filter.getNationality().toLowerCase() + "%"));
                    if (filter.getCountry() != null)
                        predicates.add(cb.like(cb.lower(root.get("country")),
                                "%" + filter.getCountry().toLowerCase() + "%"));
                    if (filter.getCreateDated() != null && !filter.getCreateDated().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreateDated().trim(), dateTimeFormatter);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        predicates.add(cb.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }
                    return cb.or(predicates.toArray(new Predicate[0]));
                }
            };
            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }
            return ApiResponse.success(toCustomerListResponse(customerRepository.findAll(spec, pageable).getContent()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customer: " + e.getMessage(), e);
        }
    }

    // Note: this function is just for grouping customer response by field, not for
    // filter or search

    private CustomerListResponse toCustomerListResponse(List<CustomerEntity> customerEntities) {
        int totalCount = (int) customerRepository.count();
        int totalActiveCount = (int) customerRepository.countByStatus(Status.ACTIVE);
        int totalInactiveCount = (int) customerRepository.countByStatus(Status.INACTIVE);
        int totalDeletedCount = (int) customerRepository.countByStatus(Status.DELETED);
        int totalLoginWithGoogle = (int) customerRepository.countByProvider(AuthProvider.GOOGLE);
        int totalLoginWithSystem = (int) customerRepository.countByProvider(AuthProvider.LOCAL);

        return CustomerListResponse.builder()

                .totalCustomers(totalCount)
                .totalActiveCustomers(totalActiveCount)
                .totalInacitveCustomers(totalInactiveCount)
                .totalDeletedCustomers(totalDeletedCount)
                .loginWithGoogle(totalLoginWithGoogle)
                .loginWithSystem(totalLoginWithSystem)
                .customers(customerEntities.stream().map(CustomerResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }
}
