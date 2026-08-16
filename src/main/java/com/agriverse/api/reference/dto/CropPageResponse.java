package com.agriverse.api.reference.dto;

import java.util.List;

public record CropPageResponse(List<CropResponse> content, long totalElements, int page) {
}
