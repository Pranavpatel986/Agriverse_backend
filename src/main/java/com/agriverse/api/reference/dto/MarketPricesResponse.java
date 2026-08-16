package com.agriverse.api.reference.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MarketPricesResponse(UUID cropId, List<MarketPriceEntry> prices, BigDecimal latestModalPrice) {
}
