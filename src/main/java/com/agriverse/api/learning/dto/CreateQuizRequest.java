package com.agriverse.api.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuizRequest(
        @NotBlank @Size(min = 5, max = 255) String title,
        String description,
        @NotNull Integer passingScore,
        @NotEmpty @Valid List<CreateQuizQuestionRequest> questions
) {
}
