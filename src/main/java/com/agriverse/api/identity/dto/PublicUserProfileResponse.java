package com.agriverse.api.identity.dto;

import java.util.UUID;

public record PublicUserProfileResponse(
        UUID id,
        String displayName,
        String bio,
        Integer articlesPublishedCount
) {
}
