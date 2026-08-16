package com.agriverse.api.content.dto;

import java.util.UUID;

public record ArticleCreatedResponse(UUID id, String slug, String status) {
}
