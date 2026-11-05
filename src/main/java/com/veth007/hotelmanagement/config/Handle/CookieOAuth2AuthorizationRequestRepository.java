package com.veth007.hotelmanagement.config.Handle;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;

/**
 * Stores OAuth2AuthorizationRequest in cookies instead of HttpSession.
 * Fixes "authorization_request_not_found" when session is lost during
 * the Google → callback redirect.
 */
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String REDIRECT_URI_COOKIE_NAME = "oauth2_redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, AUTH_REQUEST_COOKIE_NAME);
        if (cookie == null) return null;
        return deserialize(cookie.getValue());
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        if (authorizationRequest == null) {
            // Remove cookies
            deleteCookie(response, AUTH_REQUEST_COOKIE_NAME);
            deleteCookie(response, REDIRECT_URI_COOKIE_NAME);
            return;
        }
        addCookie(response, AUTH_REQUEST_COOKIE_NAME, serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        String redirectUriAfterLogin = request.getParameter("redirect_uri");
        if (redirectUriAfterLogin != null) {
            addCookie(response, REDIRECT_URI_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteCookie(response, AUTH_REQUEST_COOKIE_NAME);
        deleteCookie(response, REDIRECT_URI_COOKIE_NAME);
        return authRequest;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false); // true in production with HTTPS
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String serialize(OAuth2AuthorizationRequest obj) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(value));
        } catch (Exception e) {
            return null;
        }
    }
}
