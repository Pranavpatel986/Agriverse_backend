package com.agriverse.api.learning.dto;

import java.util.List;
import java.util.UUID;

public record RoadmapDetailResponse(UUID id, String title, List<RoadmapStepResponse> steps, RoadmapProgressResponse progress) {
}
