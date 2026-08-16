package com.agriverse.api.admin.controller;

import com.agriverse.api.admin.dto.*;
import com.agriverse.api.admin.service.AdminModerationService;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Comments moderation — REST API Specification, Section 12. EDITOR/ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminModerationService moderationService;

    @GetMapping("/queue")
    public ContentListResponse<ModerationQueueItemResponse> queue(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return moderationService.queue(pageable);
    }

    @PatchMapping("/{id}/moderate")
    public ModerateCommentResponse moderate(@PathVariable UUID id, @Valid @RequestBody ModerateCommentRequest request) {
        return moderationService.moderate(id, request);
    }
}
