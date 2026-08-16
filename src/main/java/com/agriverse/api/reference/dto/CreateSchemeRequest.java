package com.agriverse.api.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateSchemeRequest(
        @NotBlank @Size(min = 5, max = 255) String name,
        @NotBlank String description,
        @NotBlank String beneficiaryType,
        String state,
        UUID cropId,
        @NotBlank String benefitSummary,
        LocalDate applicationDeadline,
        @NotBlank String officialUrl,
        @NotBlank String source
) {
}
