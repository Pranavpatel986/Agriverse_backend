package com.agriverse.api.identity.dto;

public record RefreshTokenResponse(String accessToken, long expiresIn) {
}
