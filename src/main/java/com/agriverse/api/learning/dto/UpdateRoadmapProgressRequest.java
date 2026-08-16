package com.agriverse.api.learning.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateRoadmapProgressRequest(@NotNull UUID stepId, @NotNull Boolean completed) {
}
