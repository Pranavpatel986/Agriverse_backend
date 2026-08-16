package com.agriverse.api.engagement.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.engagement.dto.*;
import com.agriverse.api.engagement.service.BookmarkService;
import com.agriverse.api.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Bookmarks endpoint group — REST API Specification, Section 6. All endpoints require authentication. */
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public ContentListResponse<BookmarkResponse> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return bookmarkService.list(SecurityUtils.currentUserId(), entityType, pageable);
    }

    @PostMapping
    public ResponseEntity<BookmarkCreatedResponse> create(@Valid @RequestBody CreateBookmarkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkService.create(SecurityUtils.currentUserId(), request));
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        bookmarkService.delete(SecurityUtils.currentUserId(), id);
        return new MessageResponse("Bookmark removed");
    }
}
