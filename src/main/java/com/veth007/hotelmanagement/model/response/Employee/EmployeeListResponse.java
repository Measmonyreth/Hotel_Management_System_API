package com.veth007.hotelmanagement.model.response.Employee;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder 
@Data
public class EmployeeListResponse {
    // total status of employee 
    //    private Long totalActive;
    private int totalEmployees;
    private int totalActive;
    private int totalInactive;
    private int totalSuspended;
    private int totalDeleted;

    // total employee by role
    private int totalManager;
    private int totalStaff;
    private int totalAdmin;
    private List<EmployeeResponse> employees;
}
