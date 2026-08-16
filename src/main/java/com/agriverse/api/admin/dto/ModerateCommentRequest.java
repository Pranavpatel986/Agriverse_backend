package com.agriverse.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ModerateCommentRequest(@NotBlank String action) {
}
