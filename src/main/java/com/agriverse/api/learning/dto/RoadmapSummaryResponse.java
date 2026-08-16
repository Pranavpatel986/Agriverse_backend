package com.agriverse.api.learning.dto;

import java.util.UUID;

public record RoadmapSummaryResponse(UUID id, String title, String slug, String description, long stepCount) {
}
