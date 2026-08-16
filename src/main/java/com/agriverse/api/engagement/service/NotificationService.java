package com.agriverse.api.engagement.service;

import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.engagement.dto.NotificationListResponse;
import com.agriverse.api.engagement.dto.NotificationReadResponse;
import com.agriverse.api.engagement.dto.NotificationResponse;
import com.agriverse.api.engagement.entity.Notification;
import com.agriverse.api.engagement.entity.NotificationType;
import com.agriverse.api.engagement.repository.NotificationRepository;
import com.agriverse.api.identity.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Backs the Notifications endpoint group (REST API Specification, Section 8). */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse list(Long userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);
        var content = page.getContent().stream()
                .map(n -> new NotificationResponse(n.getPublicId(), n.getType().name().toLowerCase(), n.getPayload(), n.isRead(), n.getCreatedAt()))
                .toList();
        return new NotificationListResponse(content, unreadCount);
    }

    @Transactional
    public NotificationReadResponse markRead(Long userId, UUID id) {
        Notification notification = notificationRepository.findByPublicIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
        return new NotificationReadResponse(notification.getPublicId(), true);
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId, Instant.now());
    }

    @Transactional
    public void delete(Long userId, UUID id) {
        Notification notification = notificationRepository.findByPublicIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        notificationRepository.delete(notification);
    }

    /**
     * The producing side the roadmap flagged as missing entirely: until now
     * nothing ever called this, so the table + read/unread API existed with
     * no events feeding it. Called from CommentService (COMMENT_REPLY) and
     * AdminArticleService (MODERATION_STATUS) — see call sites for the
     * payload shape each event uses. Deliberately takes the recipient User
     * directly rather than a userId, so a caller that already has the
     * entity loaded (as both current call sites do) doesn't pay for an
     * extra lookup.
     */
    @Transactional
    public void create(User recipient, NotificationType type, JsonNode payload) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setPayload(payload);
        notificationRepository.save(notification);
    }
}
