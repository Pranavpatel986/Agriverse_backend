package com.agriverse.api.reference.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateMachineryRequest(
        String name, String category, String description, BigDecimal priceRangeMin,
        BigDecimal priceRangeMax, UUID applicableCropId, UUID articleId
) {
    public boolean isEmpty() {
        return name == null && category == null && description == null && priceRangeMin == null
                && priceRangeMax == null && applicableCropId == null && articleId == null;
    }
}
