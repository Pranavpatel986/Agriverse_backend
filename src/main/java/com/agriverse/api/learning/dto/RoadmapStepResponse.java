package com.agriverse.api.learning.dto;

import java.util.UUID;

public record RoadmapStepResponse(UUID id, int order, String title, String articleSlug, UUID quizId) {
}
