package com.agriverse.api.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateDiseaseRequest(
        @NotBlank String name,
        @NotBlank String pathogenType,
        @NotBlank String severity,
        @NotBlank String symptoms,
        @NotBlank String treatment,
        String prevention,
        @NotEmpty List<UUID> cropIds,
        UUID articleId
) {
}
