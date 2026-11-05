package com.veth007.hotelmanagement.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.veth007.hotelmanagement.exception.BusinessException;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long jwtRefreshExpiration;

    // TODO: Add methods for JWT generation and validation
    // - generateToken(EmployeeEntity employee)
    // - validateToken(String token)
    // - getUsernameFromToken(String token)
    // - getExpirationDateFromToken(String token)

    // public String generateRefreshToken(UserDetails userDetails) {
    // return Jwts.builder()
    // .setSubject(userDetails.getUsername())
    // .setIssuedAt(new Date())
    // .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration *
    // 1000))
    // .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
    // .compact();
    // }

    // public String generateToken(UserDetails userDetails) {
    // // TODO: Implement JWT token generation
    // return Jwts.builder()
    // .setSubject(userDetails.getUsername())
    // .claim("roles",
    // userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
    // .setIssuedAt(new Date())
    // .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
    // .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
    // .compact();
    // }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ Fixed — includes userType claim
    public String generateAccessToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();

        if (principal instanceof EmployeeDetails employeeDetails) {
            claims.put("id", employeeDetails.getEmployeeId());
            claims.put("userType", "EMPLOYEE");
            claims.put("role", employeeDetails.getRole().name());
            claims.put("employeeId", employeeDetails.getEmployeeId());
            return buildToken(claims, employeeDetails.getUsername(), jwtExpiration);

        } else if (principal instanceof CustomerDetails customerDetails) {
            claims.put("id", customerDetails.getId()); // Assuming username is unique customer ID
            claims.put("userType", "CUSTOMER");
            claims.put("role", customerDetails.getRole().name());
            claims.put("customerId", customerDetails.getCustomerId());
            return buildToken(claims, customerDetails.getUsername(), jwtExpiration);

        } else {
            throw new BusinessException("Unknown principal type");
        }
    }

    public String generateCustomerAccessToken(CustomerEntity customer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", customer.getId());
        claims.put("userType", "CUSTOMER");
        claims.put("role", customer.getRole().name());
        claims.put("customerId", customer.getCustomerId());
        return buildToken(claims, customer.getEmail(), jwtExpiration);
    }

    // ✅ Default to CUSTOMER for OAuth2 / email-based flows
    public String generateAccessTokenFromEmail(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", "CUSTOMER");
        claims.put("role", "CUSTOMER");
        return buildToken(claims, email, jwtExpiration);
    }

    // no change needed
    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, jwtRefreshExpiration);
    }

    // ✅ Fixed — accepts claims map
    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims) // 👈 userType, role, employeeId
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !getClaims(token).getExpiration().before(new Date())
                && !tokenBlacklist.isBlacklisted(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserType(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);

        if (role == null || role.isBlank()) {
            return null;
        }

        String normalizedRole = role.toUpperCase();
        if (normalizedRole.contains("EMPLOYEE") || normalizedRole.contains("ADMIN") || normalizedRole.contains("MANAGER") ||normalizedRole.contains("STAFF")  ) {
            return "EMPLOYEE";
        }
        if (normalizedRole.contains("CUSTOMER") || normalizedRole.contains("USER")) {
            return "CUSTOMER";
        }

        return null;
    }
}
