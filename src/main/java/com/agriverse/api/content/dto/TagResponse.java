package com.agriverse.api.content.dto;

import java.util.UUID;

public record TagResponse(UUID id, String name, String slug) {
}
