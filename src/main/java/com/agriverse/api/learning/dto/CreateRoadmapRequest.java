package com.agriverse.api.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRoadmapRequest(
        @NotBlank @Size(min = 5, max = 255) String title,
        @Size(max = 1000) String description,
        UUID categoryId,
        @NotEmpty @Valid List<CreateRoadmapStepRequest> steps
) {
}
