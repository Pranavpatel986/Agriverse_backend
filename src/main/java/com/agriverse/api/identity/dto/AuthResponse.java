package com.agriverse.api.identity.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserSummary user) {
}
