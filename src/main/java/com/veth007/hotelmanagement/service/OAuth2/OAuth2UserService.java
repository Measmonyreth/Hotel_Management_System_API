package com.veth007.hotelmanagement.service.OAuth2;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.veth007.hotelmanagement.config.JwtUtil;
import com.veth007.hotelmanagement.config.Validation.GmailDomainValidator;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.exception.UnauthorizedException;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.response.AuthResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService implements
        org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Value("${google.web-client-id}")
    private String webClientId;

    @Value("${google.android-client-id}")
    private String androidClientId;

    private final CustomerRepository customerRepository;
    private final GmailDomainValidator gmailDomainValidator;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");
        String googleId = (String) attributes.get("sub");
        Boolean verified = (Boolean) attributes.get("email_verified");

        log.info("OAuth2User loaded: {}", email);
        log.info("First name: {}", firstName);
        log.info("Last name: {}", lastName);
        log.info("Picture: {}", picture);
        log.info("Google ID: {}", googleId);
        log.info("Email verified: {}", verified);

        if (!gmailDomainValidator.isGoogleEmail(email)) {
            throw new OAuth2AuthenticationException("Email domain is not Gmail. Only @gmail.com is accepted.");
        }
        Optional<CustomerEntity> theLastCustomerId = customerRepository.findTopByOrderByCustomerIdDesc();
        Long lastId = 0L;

        if (theLastCustomerId.isPresent()) {
            String customerIdStr = theLastCustomerId.get().getCustomerId();
            if (customerIdStr != null && customerIdStr.contains("-")) {
                try {
                    lastId = Long.parseLong(customerIdStr.substring(customerIdStr.lastIndexOf("-") + 1));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse customer ID: {}", customerIdStr, e);
                    lastId = 0L;
                }
            }
        }

        String newCustomerId = "CUS-" + String.format("%08d", lastId + 1);

        CustomerEntity customer = customerRepository.findByEmail(email).orElse(
                CustomerEntity.builder()
                        .customerId(newCustomerId)
                        .email(email)
                        .provider(CustomerEntity.AuthProvider.GOOGLE)
                        .picture(picture)
                        .role(Role.CUSTOMER)
                        .status(Status.ACTIVE)
                        .build());

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPicture(picture);
        customer.setProviderId(googleId);
        customer.setEmailVerified(Boolean.TRUE.equals(verified));
        customer.setStatus(Status.ACTIVE);
        customerRepository.save(customer);

        log.info("OAuth2 login: {} (verified={})", email, verified);
        return oAuth2User;
    }

    public AuthResponse loginWithGoogleToken(String idToken) {
        // Verify Google ID token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Arrays.asList(webClientId, androidClientId))
                .build();
        GoogleIdToken googleIdToken;
        try {
            googleIdToken = verifier.verify(idToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Google token: " + e.getMessage());
        }

        if (googleIdToken == null)
            throw new UnauthorizedException("Invalid Google token");

        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String picture = (String) payload.get("picture");
        String googleId = payload.getSubject();
        Boolean verified = payload.getEmailVerified();

        if (!gmailDomainValidator.isGoogleEmail(email)) {
            throw new UnauthorizedException("Only @gmail.com is accepted");
        }

        // Reuse existing customer save logic
        Optional<CustomerEntity> theLastCustomerId = customerRepository.findTopByOrderByCustomerIdDesc();
        Long lastId = 0L;
        if (theLastCustomerId.isPresent()) {
            String customerIdStr = theLastCustomerId.get().getCustomerId();
            if (customerIdStr != null && customerIdStr.contains("-")) {
                try {
                    lastId = Long.parseLong(customerIdStr.substring(customerIdStr.lastIndexOf("-") + 1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String newCustomerId = "CUS-" + String.format("%08d", lastId + 1);

        CustomerEntity customer = customerRepository.findByEmail(email).orElse(
                CustomerEntity.builder()
                        .customerId(newCustomerId)
                        .email(email)
                        .provider(CustomerEntity.AuthProvider.GOOGLE)
                        .picture(picture)
                        .role(Role.CUSTOMER)
                        .status(Status.ACTIVE)
                        .build());

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPicture(picture);
        customer.setProviderId(googleId);
        customer.setEmailVerified(Boolean.TRUE.equals(verified));
        customer.setStatus(Status.ACTIVE);
        customerRepository.save(customer);

        // Generate JWT with proper claims
        String accessToken = jwtUtil.generateCustomerAccessToken(customer);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        return new AuthResponse(accessToken, refreshToken, customer.getCustomerId(), customer.getRole().name());
    }

}
