package com.agriverse.api.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuizOptionRequest(@NotBlank String optionText, @NotNull Boolean isCorrect) {
}
