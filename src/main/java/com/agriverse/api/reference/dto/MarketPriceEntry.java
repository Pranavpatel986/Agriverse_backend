package com.agriverse.api.reference.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketPriceEntry(String marketName, String state, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal modalPrice, LocalDate priceDate, String source) {
}
