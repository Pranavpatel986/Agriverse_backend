package com.agriverse.api.identity.dto;

public record SocialLoginResponse(String accessToken, String refreshToken, boolean isNewUser) {
}
