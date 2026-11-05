package com.veth007.hotelmanagement.config.Validation;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class GmailDomainValidator {
    private static final Set<String> ALLOWED_DOMAINS = Set.of("gmail.com", "googlemail.com");
    
    public boolean isGoogleEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        return ALLOWED_DOMAINS.contains(domain.toLowerCase());
    }
    public String getRejectionReason(String email) {
        if (email == null || !email.contains("@")) return "Invalid email format.";
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase().trim();
        return "Email domain '@" + domain + "' is not Gmail. Only @gmail.com is accepted.";
    }
}
