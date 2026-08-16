package com.agriverse.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticleReviewRequest(@NotBlank String decision, String feedback) {
}
