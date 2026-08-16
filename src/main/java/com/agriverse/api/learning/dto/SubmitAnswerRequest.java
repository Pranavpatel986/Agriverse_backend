package com.agriverse.api.learning.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitAnswerRequest(@NotNull UUID questionId, @NotNull UUID selectedOptionId) {
}
