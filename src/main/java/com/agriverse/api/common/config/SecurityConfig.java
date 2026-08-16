package com.agriverse.api.common.config;

import com.agriverse.api.common.dto.ApiErrorResponse;
import com.agriverse.api.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless JWT security per SAD Section 3.2: no server-side session
 * affinity, authorization enforced per-endpoint by role, ownership checks
 * enforced in the service layer (e.g. an Author editing only their own
 * article).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final CorsProperties corsProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, ex) -> writeError(response, HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) -> writeError(response, HttpStatus.FORBIDDEN)))
                .authorizeHttpRequests(auth -> auth
                        // H2 console only exists when spring.h2.console.enabled=true (test/dev profile);
                        // permitting the path is a no-op in profiles where the endpoint isn't registered at all.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh-token",
                                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", "/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification", "/api/v1/auth/social-login").permitAll()
                        // /api/v1/auth/logout requires a valid access token, per the API spec.
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/articles", "/api/v1/articles/*", "/api/v1/articles/*/related",
                                "/api/v1/articles/*/recommendations", "/api/v1/articles/*/comments",
                                "/api/v1/categories", "/api/v1/categories/*",
                                "/api/v1/search", "/api/v1/search/autocomplete", "/api/v1/search/trending",
                                "/api/v1/roadmaps", "/api/v1/roadmaps/*",
                                "/api/v1/schemes", "/api/v1/schemes/*",
                                "/api/v1/diseases", "/api/v1/diseases/*",
                                "/api/v1/market-prices", "/api/v1/weather",
                                "/api/v1/crops", "/api/v1/crops/*",
                                "/api/v1/machinery", "/api/v1/machinery/*",
                                "/api/v1/tags",
                                "/api/v1/users/*"
                        ).permitAll()
                        // Coarse gate: any admin-area endpoint needs at least EDITOR. Endpoints
                        // restricted further to ADMIN-only (user management, analytics) enforce
                        // that via @PreAuthorize("hasRole('ADMIN')") on the specific controller method.
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("EDITOR", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOriginsList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Request-Id", "Retry-After"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private Void writeError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), "n/a");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return null;
    }
}
