package com.agriverse.api.reference.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateSchemeRequest(
        String name, String description, String beneficiaryType, String state, UUID cropId,
        String benefitSummary, LocalDate applicationDeadline, String officialUrl, String source
) {
    public boolean isEmpty() {
        return name == null && description == null && beneficiaryType == null && state == null && cropId == null
                && benefitSummary == null && applicationDeadline == null && officialUrl == null && source == null;
    }
}
