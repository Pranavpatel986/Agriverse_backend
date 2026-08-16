package com.agriverse.api.reference.dto;

import java.util.UUID;

public record ArticleRefDto(UUID id, String slug, String title) {
}
