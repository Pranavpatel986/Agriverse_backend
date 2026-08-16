package com.agriverse.api.admin.dto;

import java.util.UUID;

public record AdminArticleSummaryResponse(UUID id, String title, String status, AdminAuthorRef author) {
}
