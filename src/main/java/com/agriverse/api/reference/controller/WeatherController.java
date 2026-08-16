package com.agriverse.api.reference.controller;

import com.agriverse.api.reference.dto.WeatherResponse;
import com.agriverse.api.reference.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Weather endpoint group — REST API Specification, Section 16. Public; lat/lon required unless savedLocationId given. */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) UUID savedLocationId) {
        return weatherService.getWeather(lat, lon, savedLocationId);
    }
}
