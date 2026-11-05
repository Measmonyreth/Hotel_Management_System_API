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
public class CustomerDetails implements UserDetails{
    
    private final CustomerEntity customer; // 👈 wraps your entity

    public CustomerDetails(CustomerEntity customer) {
        this.customer = customer;
    }

    // expose what you need
    public String getCustomerId() { return customer.getCustomerId(); }
    public Long getId()           { return customer.getId(); }
    public String getFullName()   { return customer.getFirstName() + " " + customer.getLastName(); }
    public Role getRole()              { return customer.getRole(); }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return customer.getEmail();
    }
    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return customer.getPassword();
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return customer.isEmailVerified(); // 👈 only allow login if email is verified
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO Auto-generated method stub
        return Stream.of(customer.getRole()).map(r -> new SimpleGrantedAuthority((r.name()))).collect(Collectors.toList());
    }

        @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return customer.getStatus() != Status.SUSPENDED; // 👈 if you have SUSPENDED status
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
