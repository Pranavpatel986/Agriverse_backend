package com.agriverse.api.reference.dto;

import java.util.List;
import java.util.UUID;

public record UpdateDiseaseRequest(
        String name, String pathogenType, String severity, String symptoms, String treatment,
        String prevention, List<UUID> cropIds, UUID articleId
) {
    public boolean isEmpty() {
        return name == null && pathogenType == null && severity == null && symptoms == null && treatment == null
                && prevention == null && cropIds == null && articleId == null;
    }
}
