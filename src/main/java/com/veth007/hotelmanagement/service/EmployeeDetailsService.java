package com.veth007.hotelmanagement.service;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
import com.veth007.hotelmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@RequiredArgsConstructor
public class EmployeeDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    // Called by Spring Security during every request to load user from DB
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        EmployeeEntity employeeEntity = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found: " + username));

        return new EmployeeDetails(employeeEntity);

    }

        // Return employee id for given username, or null if not found
        public Long getId(String username) {
                return employeeRepository.findByUsername(username)
                                .map(EmployeeEntity::getId)
                                .orElse(null);
        }
}