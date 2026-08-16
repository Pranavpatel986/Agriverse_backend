package com.agriverse.api.identity.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        String status,
        Instant createdAt,
        Instant lastLoginAt
) {
}
