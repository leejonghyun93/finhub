package com.finhub.user.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken, "Bearer");
    }
}
