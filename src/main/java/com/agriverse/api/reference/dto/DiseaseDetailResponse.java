package com.agriverse.api.reference.dto;

import java.util.List;
import java.util.UUID;

public record DiseaseDetailResponse(
        UUID id, String name, String pathogenType, String severity, String symptoms,
        String treatment, String prevention, List<CropRef> crops, ArticleRefDto article
) {
}
