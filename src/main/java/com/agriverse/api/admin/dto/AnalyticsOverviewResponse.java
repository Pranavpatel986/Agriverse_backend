package com.agriverse.api.admin.dto;

public record AnalyticsOverviewResponse(long totalUsers, long newUsers, long totalArticles, long totalViews, long pendingModeration) {
}
