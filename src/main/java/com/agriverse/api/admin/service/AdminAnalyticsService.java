package com.agriverse.api.admin.service;

import com.agriverse.api.admin.dto.AnalyticsOverviewResponse;
import com.agriverse.api.content.entity.ArticleStatus;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.engagement.entity.ModerationStatus;
import com.agriverse.api.engagement.repository.CommentRepository;
import com.agriverse.api.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Backs GET /api/v1/admin/analytics/overview (REST API Specification, Section 12). ADMIN-only. */
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse overview(LocalDate from, LocalDate to) {
        LocalDate rangeTo = to != null ? to : LocalDate.now();
        LocalDate rangeFrom = from != null ? from : rangeTo.minusDays(30);
        Instant fromInstant = rangeFrom.atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalUsers = userRepository.count();
        long newUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(fromInstant))
                .count();
        long totalArticles = articleRepository.adminSearch(ArticleStatus.PUBLISHED, null, Pageable.unpaged()).getTotalElements();
        long totalViews = articleRepository.findAll().stream()
                .filter(a -> a.getStatus() == ArticleStatus.PUBLISHED)
                .mapToLong(a -> a.getViewCount() == null ? 0 : a.getViewCount())
                .sum();
        long pendingModeration = commentRepository.findModerationQueue(Pageable.unpaged()).getTotalElements()
                + articleRepository.adminSearch(ArticleStatus.IN_REVIEW, null, Pageable.unpaged()).getTotalElements();

        return new AnalyticsOverviewResponse(totalUsers, newUsers, totalArticles, totalViews, pendingModeration);
    }
}
