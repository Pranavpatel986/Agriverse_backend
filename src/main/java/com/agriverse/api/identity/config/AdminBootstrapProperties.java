package com.agriverse.api.identity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "agriverse.admin-bootstrap")
public class AdminBootstrapProperties {
    private String email;
    private String password;
}
