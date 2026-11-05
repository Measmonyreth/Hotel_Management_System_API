package com.veth007.hotelmanagement.service.FCM_Token.Implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.model.entity.FcmTokenEntity;
import com.veth007.hotelmanagement.model.request.FCM_Token.Fcm_TokenRequest;
import com.veth007.hotelmanagement.model.request.FCM_Token.UpdateFcm_TokenRequest;
import com.veth007.hotelmanagement.model.response.FCM_Token.Fcm_TokenResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.FcmTokenRepository;
import com.veth007.hotelmanagement.service.FCM_Token.FCM_TokenService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FCM_TokenServiceImp implements FCM_TokenService {
    
    private final FcmTokenRepository fcmTokenRepository;
    private final CustomerRepository customerRepository;
    
    @Override
    public Fcm_TokenResponse saveToken(Fcm_TokenRequest request) {
        log.info("Saving FCM token");
        
        // Check if token already exists
        var existingToken = fcmTokenRepository.findByToken(request.getToken());

        var customer = request.getCustomerId() != null
                ? customerRepository.findById(request.getCustomerId()).orElse(null)
                : null;

        FcmTokenEntity token;
        if (existingToken.isPresent()) {
            token = existingToken.get();
            token.setCustomer(customer);
            token.setIsActive(true);
            token.setDeviceType(request.getDeviceType());
            log.info("Updating existing FCM token: {}", request.getToken());
        } else {
            token = FcmTokenEntity.builder()
                .customer(customer)
                .token(request.getToken())
                .deviceType(request.getDeviceType())
                .isActive(true)
                .build();
            log.info("Creating new FCM token for user: {}", request.getCustomerId());
        }
        
        FcmTokenEntity saved = fcmTokenRepository.save(token);
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Fcm_TokenResponse> getUserTokens(Long userId) {
        log.info("Fetching all tokens for user: {}", userId);
        
        List<FcmTokenEntity> tokens = fcmTokenRepository.findByCustomer_Id(userId);
        return tokens.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveTokens(Long userId) {
        log.info("Fetching active tokens for user: {}", userId);
        
        List<String> tokens = fcmTokenRepository.findActiveTokensByUserId(userId);
        log.info("Found {} active tokens for user: {}", tokens.size(), userId);
        return tokens;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fcm_TokenResponse> getAll() {
        log.info("Fetching all FCM tokens");
        
        List<FcmTokenEntity> tokens = fcmTokenRepository.findAll();
        return tokens.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Fcm_TokenResponse getTokenDetails(String token) {
        log.info("Fetching details for token: {}", token);
        
        FcmTokenEntity fcmToken = fcmTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.warn("Token not found: {}", token);
                return new RuntimeException("FCM token not found");
            });
        
        return mapToResponse(fcmToken);
    }
    
    @Override
    public Fcm_TokenResponse updateTokenStatus(UpdateFcm_TokenRequest request) {
        log.info("Updating token status: {} -> {}", request.getToken(), request.getIsActive());
        
        FcmTokenEntity token = fcmTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> {
                log.warn("Token not found: {}", request.getToken());
                return new RuntimeException("FCM token not found");
            });
        
        token.setIsActive(request.getIsActive());
        FcmTokenEntity updated = fcmTokenRepository.save(token);

        return mapToResponse(updated);
    }

    @Override
    public Fcm_TokenResponse updateTokenUserId(UpdateFcm_TokenRequest request, String email) {
        log.info("Updating token user ID: {} -> {}", request.getToken(), email);

        FcmTokenEntity token = fcmTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> {
                log.warn("Token not found: {}", request.getToken());
                return new RuntimeException("FCM token not found");
            });

        Long userId = customerRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", email);
                return new RuntimeException("User not found");
            }).getId();
        token.setCustomer(customerRepository.findById(userId).orElse(null));
        FcmTokenEntity updated = fcmTokenRepository.save(token);

        return mapToResponse(updated);
    }
    
    @Override
    public void removeToken(String token) {
        log.info("Removing token: {}", token);
        
        fcmTokenRepository.findByToken(token).ifPresent(t -> {
            fcmTokenRepository.deleteByToken(token);
            log.info("Token removed: {}", token);
        });
    }
    
    @Override
    public void removeAllUserTokens(Long userId) {
        log.info("Removing all tokens for user: {}", userId);
        
        fcmTokenRepository.deleteAllByUserId(userId);
        log.info("All tokens removed for user: {}", userId);
    }
    
    @Override
    public void deactivateAllUserTokens(Long userId) {
        log.info("Deactivating all tokens for user: {}", userId);
        
        List<FcmTokenEntity> tokens = fcmTokenRepository.findByCustomer_Id(userId);
        tokens.forEach(token -> token.setIsActive(false));
        fcmTokenRepository.saveAll(tokens);
        
        log.info("Deactivated {} tokens for user: {}", tokens.size(), userId);
    }
    
    private Fcm_TokenResponse mapToResponse(FcmTokenEntity token) {
        return Fcm_TokenResponse.fromEntity(token);
    }
}
