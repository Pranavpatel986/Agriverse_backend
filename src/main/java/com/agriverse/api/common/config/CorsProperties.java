package com.agriverse.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "agriverse.cors")
public class CorsProperties {
    private String allowedOrigins = "http://localhost:3000";

    public List<String> allowedOriginsList() {
        return Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
    }
}
