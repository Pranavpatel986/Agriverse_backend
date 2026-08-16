package com.agriverse.api.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SocialLoginRequest(
        @NotBlank @Pattern(regexp = "google|github") String provider,
        @NotBlank String providerToken
) {
}
