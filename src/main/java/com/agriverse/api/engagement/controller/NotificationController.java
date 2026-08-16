package com.agriverse.api.engagement.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.engagement.dto.NotificationListResponse;
import com.agriverse.api.engagement.dto.NotificationReadResponse;
import com.agriverse.api.engagement.dto.ReadAllResponse;
import com.agriverse.api.engagement.service.NotificationService;
import com.agriverse.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Notifications endpoint group — REST API Specification, Section 8. All endpoints require authentication. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public NotificationListResponse list(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return notificationService.list(SecurityUtils.currentUserId(), unreadOnly, pageable);
    }

    @PatchMapping("/{id}/read")
    public NotificationReadResponse markRead(@PathVariable UUID id) {
        return notificationService.markRead(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/read-all")
    public ReadAllResponse markAllRead() {
        return new ReadAllResponse(notificationService.markAllRead(SecurityUtils.currentUserId()));
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        notificationService.delete(SecurityUtils.currentUserId(), id);
        return new MessageResponse("Notification deleted");
    }
}
