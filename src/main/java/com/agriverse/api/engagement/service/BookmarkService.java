package com.agriverse.api.engagement.service;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.engagement.dto.*;
import com.agriverse.api.engagement.entity.Bookmark;
import com.agriverse.api.engagement.entity.BookmarkableType;
import com.agriverse.api.engagement.repository.BookmarkRepository;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.learning.repository.QuizRepository;
import com.agriverse.api.learning.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Backs the Bookmarks endpoint group (REST API Specification, Section 6). */
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final RoadmapRepository roadmapRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public ContentListResponse<BookmarkResponse> list(Long userId, String entityType, Pageable pageable) {
        var page = entityType == null
                ? bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : bookmarkRepository.findByUserIdAndEntityTypeOrderByCreatedAtDesc(userId, parseType(entityType), pageable);
        return new ContentListResponse<>(page.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional
    public BookmarkCreatedResponse create(Long userId, CreateBookmarkRequest request) {
        BookmarkableType type = parseType(request.entityType());
        Long entityInternalId = resolveEntityId(type, request.entityId());

        bookmarkRepository.findByUserIdAndEntityTypeAndEntityId(userId, type, entityInternalId)
                .ifPresent(b -> {
                    throw new ConflictException("Already bookmarked");
                });

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(userRepository.getReferenceById(userId));
        bookmark.setEntityType(type);
        bookmark.setEntityId(entityInternalId);
        bookmarkRepository.save(bookmark);
        return new BookmarkCreatedResponse(bookmark.getPublicId());
    }

    @Transactional
    public void delete(Long userId, UUID publicId) {
        Bookmark bookmark = bookmarkRepository.findByPublicIdAndUserId(publicId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Bookmark", publicId));
        bookmarkRepository.delete(bookmark);
    }

    private BookmarkableType parseType(String value) {
        try {
            return BookmarkableType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("entityType must be one of: article, roadmap, quiz");
        }
    }

    private Long resolveEntityId(BookmarkableType type, UUID publicId) {
        return switch (type) {
            case ARTICLE -> articleRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Article", publicId)).getId();
            case ROADMAP -> roadmapRepository.findByPublicId(publicId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Roadmap", publicId)).getId();
            case QUIZ -> quizRepository.findByPublicId(publicId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Quiz", publicId)).getId();
        };
    }

    private BookmarkResponse toResponse(Bookmark bookmark) {
        BookmarkEntitySummary summary = switch (bookmark.getEntityType()) {
            case ARTICLE -> articleRepository.findById(bookmark.getEntityId())
                    .map(a -> new BookmarkEntitySummary(a.getTitle(), a.getSlug())).orElse(null);
            case ROADMAP -> roadmapRepository.findById(bookmark.getEntityId())
                    .map(r -> new BookmarkEntitySummary(r.getTitle(), r.getSlug())).orElse(null);
            case QUIZ -> quizRepository.findById(bookmark.getEntityId())
                    .map(q -> new BookmarkEntitySummary(q.getTitle(), null)).orElse(null);
        };
        return new BookmarkResponse(bookmark.getPublicId(), bookmark.getEntityType().name().toLowerCase(),
                resolvePublicId(bookmark), summary, bookmark.getCreatedAt());
    }

    private UUID resolvePublicId(Bookmark bookmark) {
        return switch (bookmark.getEntityType()) {
            case ARTICLE -> articleRepository.findById(bookmark.getEntityId()).map(a -> a.getPublicId()).orElse(null);
            case ROADMAP -> roadmapRepository.findById(bookmark.getEntityId()).map(r -> r.getPublicId()).orElse(null);
            case QUIZ -> quizRepository.findById(bookmark.getEntityId()).map(q -> q.getPublicId()).orElse(null);
        };
    }
}
