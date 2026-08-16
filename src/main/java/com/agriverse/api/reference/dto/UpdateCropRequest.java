package com.agriverse.api.reference.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCropRequest(
        @Size(min = 2, max = 100) String name,
        @Size(max = 150) String scientificName,
        UUID categoryId
) {
    public boolean isEmpty() {
        return name == null && scientificName == null && categoryId == null;
    }
}
