package com.agriverse.api.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCropRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 150) String scientificName,
        UUID categoryId
) {
}
