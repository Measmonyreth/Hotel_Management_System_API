package com.veth007.hotelmanagement.service.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.rpc.context.AttributeContext.Auth;
import com.veth007.hotelmanagement.config.JwtUtil;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.exception.UnauthorizedException;
import com.veth007.hotelmanagement.model.entity.AddressEntity;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
import com.veth007.hotelmanagement.model.entity.ShiftEntity;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.LoginRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.RegisterRequest;
import com.veth007.hotelmanagement.model.request.EmployeeRequest.UpdateEmployeeRequest;
import com.veth007.hotelmanagement.model.response.AuthResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeListResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.repository.EmployeeRepository;
import com.veth007.hotelmanagement.repository.ScheduleRepository;
import com.veth007.hotelmanagement.repository.ShiftRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ShiftRepository shiftRepository;

    // LOGIN
    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        // 1. Verify username + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        // 2. Load employee
        EmployeeEntity employee = employeeRepository
                .findByUsername(request.email())
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        // 3. Generate JWT token
        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());

        EmployeeResponse employeeResponse = EmployeeResponse.fromEntity(employee, true, true);
        return ApiResponse.success(new AuthResponse(accessToken, refreshToken, "Bearer", employeeResponse));
    }

    // REGISTER
    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        // 1. Check duplicate username
        if (employeeRepository.existsByUsername(request.getUsername())) {
            if (employeeRepository.existsByUsernameAndStatus(request.getUsername(), Status.ACTIVE)) {
                throw new AlreadyExistException("Employee with username already exists");
            } else {
                throw new AlreadyExistException(
                        "Employee with username already exists but is deleted. Please contact admin to restore.");
            }
        }

        if (employeeRepository.existsByPhone(request.getPhone())) {
            throw new AlreadyExistException("Employee with phone already exists");
        }

        // 2. Find schedule if provided
        ShiftEntity shift = null;
        if (request.getScheduleId() != null) {
            shift = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new NotFoundException("Schedule not found"));
        }

        Optional<EmployeeEntity> lastEmployeeOpt = employeeRepository.findTopByOrderByEmployeeIdDesc();
        Long lastId = 0L;
        if (lastEmployeeOpt.isPresent() && lastEmployeeOpt.get().getEmployeeId().contains("-")) {
            String lastEmployeeId = lastEmployeeOpt.get().getEmployeeId();
            try {
                lastId = Long.parseLong(lastEmployeeId.substring(lastEmployeeId.lastIndexOf("-") + 1));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid employee ID format in database: " + lastEmployeeId);
            }
        }

        String newEmployeeId = String.format("EMP-%05d", lastId + 1);
      
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof EmployeeDetails employeeDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }

        Role currentRole = employeeDetails.getRole();

        if (currentRole == Role.ADMIN) {
            if (request.getRole() == Role.ADMIN) {
                throw new UnauthorizedException("You are not authorized to create an ADMIN employee");
            }
        } else if (currentRole == Role.MANAGER) {
            if (request.getRole() == Role.MANAGER) {
                throw new UnauthorizedException("You are not authorized to create a MANAGER employee");
            }
        } else {
            throw new UnauthorizedException("You are not authorized to create an employee");
        }

        // 3. Build and save employee
        EmployeeEntity employee = EmployeeEntity.builder()
                .employeeId(newEmployeeId)
                .name(request.getName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status(Status.ACTIVE)
                .shift(request.getScheduleId() != null ? shift : null)
                .build();

        // 4. Build and set address with employee reference if provided
        if (request.getAddress() != null) {
            AddressEntity address = AddressEntity.builder()
                    .village(request.getAddress().getVillage())
                    .commun(request.getAddress().getCommun())
                    .city_or_province(request.getAddress().getCity_or_province())
                    .country(request.getAddress().getCountry())
                    .employee(employee)
                    .build();
            employee.setAddress(address);
        }

        employeeRepository.save(employee);
        return ApiResponse.success("Employee registered successfully");
    }

    @Override
    public ApiResponse<ValidationResponse> isUsernameAvailable(String username) {
        return employeeRepository.findByUsername(username)
                .map(employee -> ApiResponse.ok(
                        employee.getStatus() == Status.ACTIVE
                                ? "Username belongs to an active employee"
                                : "Username belongs to an inactive employee",
                        new ValidationResponse(false, employee.getStatus().name(), username)))
                .orElseGet(() -> ApiResponse.ok(
                        "Username is available",
                        new ValidationResponse(true, "AVAILABLE", username)));
    }

    @Override
    public ApiResponse<ValidationResponse> isPhoneAvailable(String phone) {
        return employeeRepository.findByPhone(phone)
                .map(employee -> ApiResponse.ok(
                        employee.getStatus() == Status.ACTIVE
                                ? "Phone belongs to an active employee"
                                : "Phone belongs to an inactive employee",
                        new ValidationResponse(false, employee.getStatus().name(), phone)))
                .orElseGet(() -> ApiResponse.ok(
                        "Phone is available",
                        new ValidationResponse(true, "AVAILABLE", phone)));
    }

    @Override
    public ApiResponse<ValidationResponse> validateStatus(Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + employeeId));

        Status status = employee.getStatus();
        boolean active = status == Status.ACTIVE;
        String message = active
                ? "Employee is active"
                : "Employee is " + status.name().toLowerCase();

        return ApiResponse.ok(message,
                new ValidationResponse(active, status.name(), employeeId.toString()));
    }

    @Override
    public ApiResponse<EmployeeResponse> getProfile(UserDetails username) {
        if (username == null || username.getUsername() == null || username.getUsername().isEmpty()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        // invalid token
        if (username.getUsername().isEmpty()) {
            throw new RuntimeException("Invalid token");
        }

        EmployeeEntity employee = employeeRepository.findByUsername(username.getUsername())
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        return ApiResponse.success(EmployeeResponse.fromEntity(employee, true, true));
    }

    @Override
    public ApiResponse<EmployeeListResponse> getAllEmployees(int page, int size, boolean isPage) {
        try {
            List<EmployeeEntity> employeeEntities;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
                employeeEntities = employeeRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
                employeeEntities = employeeRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(TolistResponse(employeeEntities));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<EmployeeResponse> getEmployeeById(Long id) {
        try {
            EmployeeEntity employeeEntity = this.employeeRepository.findById(id).orElseThrow(
                    () -> new RuntimeException("Employee not found"));
            return ApiResponse.success(EmployeeResponse.fromEntity(employeeEntity, true, true));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get customer by id: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<EmployeeResponse> updateEmployee(UpdateEmployeeRequest request, Long id) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isEmpty()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        try {
            EmployeeEntity employeeEntity = this.findOne(id);
            employeeEntity.setName(
                    request.getName() != null ? request.getName() : employeeEntity.getName());

            if (request.getUsername() != null
                    && !request.getUsername().equals(employeeEntity.getUsername())
                    && employeeRepository.existsByUsername(request.getUsername())) {
                throw new AlreadyExistException("Employee with username already exists");
            }

            if (request.getPhone() != null
                    && !request.getPhone().equals(employeeEntity.getPhone())
                    && employeeRepository.existsByPhone(request.getPhone())) {
                throw new AlreadyExistException("Employee with phone already exists");
            }

            employeeEntity.setUsername(
                    request.getUsername() != null ? request.getUsername() : employeeEntity.getUsername());

            employeeEntity.setPhone(
                    request.getPhone() != null ? request.getPhone() : employeeEntity.getPhone());

            employeeEntity.setRole(
                    request.getRole() != null ? Role.valueOf(request.getRole()) : employeeEntity.getRole());

            employeeEntity.setStatus(
                    request.getStatus() != null ? Status.valueOf(request.getStatus()) : employeeEntity.getStatus());

            employeeEntity.setShift(
                    request.getSchedule() != null
                            ? shiftRepository.findById(request.getSchedule())
                                    .orElseThrow(() -> new NotFoundException("Shift not found"))
                            : employeeEntity.getShift());

            employeeEntity.setLastModifiedBy(username);
            employeeEntity.setLastModifiedDate(LocalDateTime.now());
            AddressEntity addressEntity = employeeEntity.getAddress();

            if (request.getAddress() != null) {
                if (addressEntity == null) {
                    addressEntity = new AddressEntity();
                    addressEntity.setEmployee(employeeEntity);
                }

                addressEntity.setVillage(
                        request.getAddress().getVillage() != null
                                ? request.getAddress().getVillage()
                                : addressEntity.getVillage());

                addressEntity.setCommun(
                        request.getAddress().getCommun() != null
                                ? request.getAddress().getCommun()
                                : addressEntity.getCommun());

                addressEntity.setCity_or_province(
                        request.getAddress().getCity_or_province() != null
                                ? request.getAddress().getCity_or_province()
                                : addressEntity.getCity_or_province());

                addressEntity.setCountry(
                        request.getAddress().getCountry() != null
                                ? request.getAddress().getCountry()
                                : addressEntity.getCountry());

                employeeEntity.setAddress(addressEntity);
            }

            employeeRepository.save(employeeEntity);
            return ApiResponse.success(EmployeeResponse.fromEntity(employeeEntity, true, true));

        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    private EmployeeEntity findOne(Long id) {
        if (this.employeeRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Employee has been deleted");
        }
        return this.employeeRepository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    // @Override
    // public ApiResponse<EmployeeListResponse> searchEmployees(Map<String, String>
    // allParams,
    // UpdateEmployeeRequest filter, String sort) {
    // try {
    // return searchEmployeesWithKeyword(allParams, filter, null, sort);
    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Failed to search customer: " + e.getMessage(),
    // e);
    // }
    // }

    @Override
    public ApiResponse<EmployeeListResponse> searchEmployeesWithKeyword(Map<String, String> allParams,
            UpdateEmployeeRequest filter, String search, String sort, int page, int size, boolean isPage) {
        try {
            List<EmployeeGroupBy> groupBies = new ArrayList<>();

            allParams.forEach((key, value) -> {
                try {
                    String enumKey = key;
                    if (key.contains(".")) {
                        String[] parts = key.split("\\.");
                        enumKey = parts[parts.length - 1];
                    }
                    EmployeeGroupBy groupBy = EmployeeGroupBy.valueOf(enumKey.toUpperCase());
                    groupBies.add(groupBy);
                    groupBy.applyFilter(filter, value);
                } catch (Exception e) {
                    try {
                        switch (key.toLowerCase()) {
                            case "name" -> filter.setName(value);
                            case "username" -> filter.setUsername(value);
                            case "phone" -> filter.setPhone(value);
                            default -> {
                                // ignore unknown params
                            }
                        }
                    } catch (Exception es) {
                        throw new RuntimeException("Error when search employee: " + es.getMessage());
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

            Specification<EmployeeEntity> spec = (root, query, cb) -> {
                if (search == null || search.isEmpty()) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (filter.getName() != null)
                        predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
                    if (filter.getUsername() != null)
                        predicates.add(cb.like(cb.lower(root.get("username")),
                                "%" + filter.getUsername().toLowerCase() + "%"));
                    if (filter.getPhone() != null)
                        predicates
                                .add(cb.like(cb.lower(root.get("phone")), "%" + filter.getPhone().toLowerCase() + "%"));
                    if (filter.getRole() != null)
                        predicates.add(cb.equal(root.get("role"), Role.valueOf(filter.getRole().toUpperCase())));
                    if (filter.getStatus() != null)
                        predicates.add(cb.equal(root.get("status"), Status.valueOf(filter.getStatus().toUpperCase())));
                    if (filter.getSchedule() != null)
                        predicates.add(cb.equal(root.get("shift").get("id"), filter.getSchedule()));
                    if (filter.getCreatedDate() != null && !filter.getCreatedDate().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreatedDate().trim(), dateTimeFormatter);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        predicates.add(cb.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }

                    return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));

                } else {
                    List<Predicate> predicates = new ArrayList<>();
                    String searchTerm = "%" + search.toLowerCase() + "%";
                    // search term: OR across text fields
                    Predicate searchPredicate = cb.or(
                            cb.like(cb.lower(root.get("name")), searchTerm),
                            cb.like(cb.lower(root.get("username")), searchTerm),
                            cb.like(cb.lower(root.get("phone")), searchTerm));
                    predicates.add(searchPredicate);

                    // filter predicates: AND on top of search
                    if (filter.getRole() != null)
                        predicates.add(cb.equal(root.get("role"), Role.valueOf(filter.getRole().toUpperCase())));
                    if (filter.getStatus() != null)
                        predicates.add(cb.equal(root.get("status"), Status.valueOf(filter.getStatus().toUpperCase())));
                    if (filter.getSchedule() != null)
                        predicates.add(cb.equal(root.get("shift").get("id"), filter.getSchedule()));
                    if (filter.getCreatedDate() != null && !filter.getCreatedDate().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreatedDate().trim(), dateTimeFormatter);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        predicates.add(cb.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));

                }
            };
            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }
            return ApiResponse.success(TolistResponse(employeeRepository.findAll(spec, pageable).getContent()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to search customer: " + e.getMessage(), e);
        }
    }

    // DELETE
    @Override
    public ApiResponse<String> deleteEmployee(Long id) {
        EmployeeEntity employee = this.findOne(id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        employee.setStatus(Status.DELETED);
        employee.setDeletedDate(LocalDateTime.now());
        employee.setDeletedBy(username);
        employeeRepository.save(employee);
        return ApiResponse.success("Employee deleted successfully");
    }

    // RESTORE
    @Override
    public ApiResponse<String> restoreEmployee(Long id) {
        try {
            if (this.employeeRepository.existsByIdAndDeletedDateIsNull(id)) {
                throw new AlreadyExistException("Employee is not deleted");
            }

            EmployeeEntity employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Employee not found"));

            employee.setStatus(Status.ACTIVE);
            employee.setDeletedDate(null);
            employee.setDeletedBy(null);
            employeeRepository.save(employee);
            return ApiResponse.success("Employee restored successfully");
        } catch (NotFoundException | AlreadyExistException ex) {
            throw ex;
        }
    }

    private EmployeeListResponse TolistResponse(List<EmployeeEntity> employeeEntities) {
        try {
            EmployeeRepository employeeRepository = this.employeeRepository;
            int total = (int) employeeRepository.count();
            int countActive = employeeRepository.countByStatus(Status.ACTIVE);
            int countInactive = employeeRepository.countByStatus(Status.INACTIVE);
            int countSuspended = employeeRepository.countByStatus(Status.SUSPENDED);
            int countDeleted = employeeRepository.countByStatus(Status.DELETED);

            int taotalAdmin = employeeRepository.countByRole(Role.ADMIN);
            int totalStaff = employeeRepository.countByRole(Role.STAFF);
            int totalManager = employeeRepository.countByRole(Role.MANAGER);

            List<EmployeeResponse> employeeResponses = employeeEntities.stream().map(EmployeeResponse::fromEntity)
                    .toList();

            return EmployeeListResponse.builder()
                    .totalEmployees(total)
                    .totalActive(countActive)
                    .totalInactive(countInactive)
                    .totalSuspended(countSuspended)
                    .totalDeleted(countDeleted)
                    .totalAdmin(taotalAdmin)
                    .totalStaff(totalStaff)
                    .totalManager(totalManager)
                    .employees(employeeResponses)
                    .build();
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to convert customer entities to list responses: " + e.getMessage(), e);
        }
    }
}
