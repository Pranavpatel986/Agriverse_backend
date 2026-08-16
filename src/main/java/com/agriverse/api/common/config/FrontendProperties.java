package com.agriverse.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Where the frontend lives -- used to build real clickable links in transactional emails. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "agriverse.frontend")
public class FrontendProperties {
    private String baseUrl = "http://localhost:3000";
}
