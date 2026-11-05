package com.veth007.hotelmanagement.config;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.veth007.hotelmanagement.model.entity.EmployeeDetails;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // For unauthenticated requests (self-registration), track as "SELF_REGISTERED"
            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of("SELF_REGISTERED");
            }

            Object principal = authentication.getPrincipal();

            // Handle EmployeeDetails (custom implementation)
            if (principal instanceof EmployeeDetails employeeDetails) {
                String empId = employeeDetails.getEmployeeId();
                return Optional.of(empId != null ? empId : employeeDetails.getUsername());
            }

            // Handle generic UserDetails (customers and others)
            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();
                return Optional.of(username != null ? username : "SYSTEM");
            }

            // Fallback for other authenticated users
            return Optional.of(authentication.getName() != null ? authentication.getName() : "SYSTEM");
        }
    }
}
