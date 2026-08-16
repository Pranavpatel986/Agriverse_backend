package com.agriverse.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AgriVerse Backend - Spring Boot Modular Monolith.
 *
 * Modules (per Software Architecture Document, Section 2): Identity, Content,
 * Engagement, Learning, Reference, Analytics, Search, Recommendation, Admin.
 * Each module follows a strict Controller -> Service -> Repository layering,
 * with DTOs at the boundary, as mandated by SAD Section 2.2.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.agriverse.api")
@EntityScan(basePackages = "com.agriverse.api")
@EnableCaching
@EnableAsync
@EnableScheduling
public class AgriverseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriverseApplication.class, args);
    }
}
