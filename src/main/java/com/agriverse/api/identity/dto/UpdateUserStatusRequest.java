package com.agriverse.api.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserStatusRequest(@NotBlank String status, String reason) {
}
