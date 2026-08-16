package com.agriverse.api.search.dto;

import com.agriverse.api.content.dto.CategoryRef;

import java.util.UUID;

public record SearchResultItem(UUID id, String title, String slug, String excerpt, String highlightedSnippet, CategoryRef category) {
}
