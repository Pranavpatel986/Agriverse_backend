package com.agriverse.api.reference.entity;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Durable fallback cache of third-party weather API responses; primarily served from Redis. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "weather_cache")
public class WeatherCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_key", nullable = false, unique = true, length = 150)
    private String locationKey;

    @Column(name = "region_name", nullable = false, length = 150)
    private String regionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "forecast_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode forecastPayload;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
