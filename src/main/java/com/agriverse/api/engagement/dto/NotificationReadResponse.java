package com.agriverse.api.engagement.dto;

import java.util.UUID;

public record NotificationReadResponse(UUID id, boolean isRead) {
}
