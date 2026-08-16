package com.agriverse.api.reference.dto;

import java.util.List;
import java.util.UUID;

public record DiseaseSummaryResponse(UUID id, String name, String pathogenType, String severity, List<CropRef> crops) {
}
