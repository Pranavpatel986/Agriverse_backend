package com.agriverse.api.reference.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MachineryDetailResponse(
        UUID id, String name, String category, String description,
        BigDecimal priceRangeMin, BigDecimal priceRangeMax, CropRef applicableCrop, ArticleRefDto article
) {
}
