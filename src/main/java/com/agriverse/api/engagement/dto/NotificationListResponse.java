package com.agriverse.api.engagement.dto;

import java.util.List;

public record NotificationListResponse(List<NotificationResponse> content, long unreadCount) {
}
