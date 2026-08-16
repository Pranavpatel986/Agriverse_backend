package com.agriverse.api.reference.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SchemeDetailResponse(
        UUID id, String name, String description, String beneficiaryType, String state,
        CropRef crop, String benefitSummary, LocalDate applicationDeadline,
        String officialUrl, String source, Instant lastVerifiedAt
) {
}
