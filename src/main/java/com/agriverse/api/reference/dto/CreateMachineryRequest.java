package com.agriverse.api.reference.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMachineryRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String description,
        BigDecimal priceRangeMin,
        BigDecimal priceRangeMax,
        UUID applicableCropId,
        UUID articleId
) {
}
