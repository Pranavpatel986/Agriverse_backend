package com.agriverse.api.identity.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 150) String fullName,
        String avatarUrl,
        @Size(max = 10) String locale,
        @Size(max = 20) String phone
) {
    public boolean isEmpty() {
        return fullName == null && avatarUrl == null && locale == null && phone == null;
    }
}
