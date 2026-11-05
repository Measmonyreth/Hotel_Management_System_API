package com.veth007.hotelmanagement.service.FCM_Token;
import com.veth007.hotelmanagement.model.request.FCM_Token.Fcm_TokenRequest;
import com.veth007.hotelmanagement.model.request.FCM_Token.UpdateFcm_TokenRequest;
import com.veth007.hotelmanagement.model.response.FCM_Token.Fcm_TokenResponse;
import java.util.List;
 
public interface FCM_TokenService {
    
    /**
     * Save or update FCM token for a user
     */
    Fcm_TokenResponse saveToken(Fcm_TokenRequest request);
    
    /**
     * Get all active tokens for a user
     */
    List<Fcm_TokenResponse> getUserTokens(Long userId);
    
    /**
     * Get only token strings (for sending notifications)
     */
    List<String> getActiveTokens(Long userId);
    
    /**
     * Get specific token details
     */
    Fcm_TokenResponse getTokenDetails(String token);

    List<Fcm_TokenResponse> getAll();
    
    /**
     * Update token status
     */
    Fcm_TokenResponse updateTokenStatus(UpdateFcm_TokenRequest request);
    
    Fcm_TokenResponse updateTokenUserId(UpdateFcm_TokenRequest request, String email);
    /**
     * Remove a specific token
     */
    void removeToken(String token);
    
    /**
     * Remove all tokens for a user (logout)
     */
    void removeAllUserTokens(Long userId);
    
    /**
     * Mark all user tokens as inactive
     */
    void deactivateAllUserTokens(Long userId);
}
