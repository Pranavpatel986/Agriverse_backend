package com.agriverse.api.engagement.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String type, JsonNode payload, boolean isRead, Instant createdAt) {
}
