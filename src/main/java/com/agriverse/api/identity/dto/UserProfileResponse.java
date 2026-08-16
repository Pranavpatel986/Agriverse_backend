package com.agriverse.api.identity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        String avatarUrl,
        String locale,
        Instant createdAt
) {
}
