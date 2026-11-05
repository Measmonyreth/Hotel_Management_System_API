package com.veth007.hotelmanagement.model.response;


public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Object user
) {


}
