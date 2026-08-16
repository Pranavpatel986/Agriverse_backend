package com.agriverse.api.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuizQuestionRequest(@NotBlank String questionText, @Valid @Size(min = 2) List<CreateQuizOptionRequest> options) {
}
