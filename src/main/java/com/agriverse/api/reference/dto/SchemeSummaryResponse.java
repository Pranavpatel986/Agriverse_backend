package com.agriverse.api.reference.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SchemeSummaryResponse(UUID id, String name, String beneficiaryType, String state, String benefitSummary, LocalDate applicationDeadline) {
}
