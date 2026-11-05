package com.veth007.hotelmanagement.model.entity;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDetails implements UserDetails {

    private final EmployeeEntity employee; // 👈 wraps your entity

    public EmployeeDetails(EmployeeEntity employee) {
        this.employee = employee;
    }

    // expose what you need
    public String getEmployeeId() { return employee.getEmployeeId(); }
    public Long getId()           { return employee.getId(); }
    public String getFullName()   { return employee.getName(); }
    public Role getRole()              { return employee.getRole(); }

    // UserDetails required
    @Override public String getUsername() { return employee.getUsername(); }
    @Override public String getPassword() { return employee.getPassword(); }
    @Override public boolean isEnabled()  { return employee.getStatus() == Status.ACTIVE; }

    // ...
  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(employee.getRole())
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return employee.getStatus() != Status.SUSPENDED; // 👈 if you have SUSPENDED status
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
