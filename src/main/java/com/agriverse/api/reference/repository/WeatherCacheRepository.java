package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {
    Optional<WeatherCache> findByLocationKey(String locationKey);
}
