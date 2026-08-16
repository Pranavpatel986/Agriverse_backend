package com.agriverse.api.reference.service;

import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.reference.dto.WeatherResponse;
import com.agriverse.api.reference.entity.WeatherCache;
import com.agriverse.api.reference.repository.WeatherCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

/**
 * Backs the single Weather endpoint (REST API Specification, Section 16).
 * Implements the fallback chain from the Software Architecture Document,
 * Section 16: Redis (via {@code @Cacheable}) -> Weather_Cache table ->
 * external provider. The provider call itself
 * ({@link #fetchFromProvider}) is an integration seam — wire in a real
 * weather API client (with an API key) without touching callers.
 */
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final long CACHE_TTL_HOURS = 3;

    private final WeatherCacheRepository weatherCacheRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public WeatherResponse getWeather(Double lat, Double lon, UUID savedLocationId) {
        if (savedLocationId != null) {
            // Saved locations are a Phase 2+ feature not yet in the Database Design
            // Specification's 30-entity inventory; every savedLocationId therefore
            // fails the "belongs to the authenticated user" check per the API
            // contract's own 404 error case.
            throw new ResourceNotFoundException("savedLocationId does not belong to the authenticated user");
        }
        if (lat == null || lon == null) {
            throw new BadRequestException("Either (lat and lon) or savedLocationId must be provided");
        }
        return cachedWeather(lat, lon);
    }

    /**
     * Checks the durable Weather_Cache table (Database Design Specification,
     * Section 5) and refreshes from the provider on expiry. In production
     * this method's result should additionally sit behind a Redis-backed
     * {@code @Cacheable} on a *separate* Spring bean — self-invocation
     * within one class silently bypasses Spring AOP proxies, so that layer
     * is intentionally left as a follow-up rather than added here in a way
     * that would look functional but do nothing.
     */
    public WeatherResponse cachedWeather(Double lat, Double lon) {
        String locationKey = locationKey(lat, lon);
        WeatherCache cached = weatherCacheRepository.findByLocationKey(locationKey).orElse(null);

        if (cached != null && cached.getExpiresAt().isAfter(Instant.now())) {
            return toResponse(cached);
        }

        WeatherCache refreshed = fetchFromProvider(lat, lon, locationKey);
        weatherCacheRepository.save(refreshed);
        return toResponse(refreshed);
    }

    /** Placeholder provider call — replace with a real weather API integration. */
    private WeatherCache fetchFromProvider(double lat, double lon, String locationKey) {
        ObjectNode forecast = objectMapper.createObjectNode();
        forecast.put("tempC", 28);
        forecast.put("condition", "Partly Cloudy");
        forecast.put("humidity", 60);

        WeatherCache cache = new WeatherCache();
        cache.setLocationKey(locationKey);
        cache.setRegionName(String.format(Locale.ROOT, "Lat %.4f, Lon %.4f", lat, lon));
        cache.setForecastPayload(forecast);
        cache.setFetchedAt(Instant.now());
        cache.setExpiresAt(Instant.now().plus(CACHE_TTL_HOURS, ChronoUnit.HOURS));
        return cache;
    }

    private String locationKey(double lat, double lon) {
        return String.format(Locale.ROOT, "%.2f,%.2f", lat, lon);
    }

    private WeatherResponse toResponse(WeatherCache cache) {
        return new WeatherResponse(cache.getRegionName(), cache.getForecastPayload(), cache.getFetchedAt(), cache.getExpiresAt());
    }
}
