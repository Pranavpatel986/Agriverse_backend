package com.agriverse.api.search.dto;

import java.util.List;

public record SearchResponse(List<SearchResultItem> content, long totalElements, long tookMs) {
}
