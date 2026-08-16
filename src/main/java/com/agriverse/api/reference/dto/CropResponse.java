package com.agriverse.api.reference.dto;

import java.util.UUID;

public record CropResponse(UUID id, String name, String scientificName, UUID categoryId) {
}
