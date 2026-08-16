package com.agriverse.api.identity.dto;

import java.util.UUID;

public record UserSummary(UUID id, String fullName, String role) {
}
