package com.agriverse.api.reference.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MachinerySummaryResponse(UUID id, String name, String category, BigDecimal priceRangeMin, BigDecimal priceRangeMax) {
}
