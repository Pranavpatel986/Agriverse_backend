package com.agriverse.api.reference.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record WeatherResponse(String regionName, JsonNode forecast, Instant fetchedAt, Instant expiresAt) {
}
