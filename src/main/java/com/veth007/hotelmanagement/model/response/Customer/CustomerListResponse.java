package com.veth007.hotelmanagement.model.response.Customer;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerListResponse {
    private int totalCustomers;
    private int totalActiveCustomers;
    private int totalInacitveCustomers;
    private int totalDeletedCustomers;
    private int loginWithGoogle;
    private int loginWithSystem;
    private List<CustomerResponse> customers;
}
