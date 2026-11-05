// package com.veth007.hotelmanagement.controller;

// import java.util.List;

// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.http.ResponseEntity;

// import com.veth007.hotelmanagement.config.HashIdUtil;
// import com.veth007.hotelmanagement.constant.enums.Status;
// import com.veth007.hotelmanagement.constant.enums.Role;
// import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
// import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;
// import com.veth007.hotelmanagement.repository.EmployeeRepository;

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/employee")
// @RequiredArgsConstructor
// public class EmployeeController {

//     private final EmployeeRepository employeeRepository;

//     // GET /api/employee/me — get current logged-in employee
//     @GetMapping("/me")
//     public ResponseEntity<EmployeeResponse> getMyProfile(
//             @AuthenticationPrincipal EmployeeEntity employee) { // @AuthenticationPrincipal automatically gets the authenticated employee profile
//         return ResponseEntity.ok(EmployeeResponse.fromEntity(employee));
//     }

//     // GET /api/employee/all — MANAGER and ADMIN only
//     @GetMapping("/all")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//     public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
//         List<EmployeeResponse> employees = employeeRepository.findAll()
//             .stream()
//             .map(EmployeeResponse::fromEntity)
//             .toList();
//         return ResponseEntity.ok(employees);
//     }

//     // GET /api/employee/role/{role}
//     @GetMapping("/role/{role}")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//     public ResponseEntity<List<EmployeeResponse>> getByRole(
//             @PathVariable Role role) {
//         List<EmployeeResponse> employees = employeeRepository.findByRole(role)
//             .stream()
//             .map(EmployeeResponse::fromEntity)
//             .toList();
//         return ResponseEntity.ok(employees);
//     }

//     // GET /api/employee/status/{status}
//     @GetMapping("/status/{status}")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//     public ResponseEntity<List<EmployeeResponse>> getByStatus(
//             @PathVariable Status status) {
//         List<EmployeeResponse> employees = employeeRepository.findByStatus(status)
//             .stream()
//             .map(EmployeeResponse::fromEntity)
//             .toList();
//         return ResponseEntity.ok(employees);
//     }

//     // DELETE /api/employee/{id} — ADMIN only
//     @DeleteMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<String> deleteEmployee(@PathVariable String id) {
//         employeeRepository.deleteById(HashIdUtil.resolveId(id));
//         return ResponseEntity.ok("Employee deleted successfully");
//     }

//     // GET /api/employee/{id} — ADMIN and MANAGER only
// }
