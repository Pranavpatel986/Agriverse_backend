package com.agriverse.api.learning.dto;

import java.util.UUID;

public record RoadmapDetailedProgressResponse(int completedSteps, UUID currentStepId, boolean isCompleted) {
}
