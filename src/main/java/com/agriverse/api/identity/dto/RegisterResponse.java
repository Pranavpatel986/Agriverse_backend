package com.agriverse.api.identity.dto;

import java.util.UUID;

public record RegisterResponse(UUID id, String fullName, String email, String status) {
}
