package com.agriverse.api.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRoadmapStepRequest(@NotNull Integer order, @NotBlank String title, UUID articleId, UUID quizId) {
}
