package com.agriverse.api.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        UUID parentCategoryId,
        @Size(max = 1000) String description
) {
}
