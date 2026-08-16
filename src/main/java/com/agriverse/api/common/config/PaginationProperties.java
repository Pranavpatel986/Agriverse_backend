package com.agriverse.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "agriverse.pagination")
public class PaginationProperties {
    private int defaultPageSize = 20;
    private int maxPageSize = 50;
}
