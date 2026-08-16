package com.agriverse.api.content.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateArticleStatusRequest(@NotBlank String status) {
}
