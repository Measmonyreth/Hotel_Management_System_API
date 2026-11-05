package com.veth007.hotelmanagement.service;

import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        CustomerEntity customerEntity = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));
        return new CustomerDetails(customerEntity);
    }

    public Long getId(String email) {
        return customerRepository.findByEmail(email)
                .map(CustomerEntity::getId)
                .orElse(null);
    }

    
}
