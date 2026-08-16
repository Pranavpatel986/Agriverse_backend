package com.agriverse.api.engagement.repository;

import com.agriverse.api.engagement.entity.Bookmark;
import com.agriverse.api.engagement.entity.BookmarkableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Bookmark> findByUserIdAndEntityTypeOrderByCreatedAtDesc(Long userId, BookmarkableType entityType, Pageable pageable);
    Optional<Bookmark> findByUserIdAndEntityTypeAndEntityId(Long userId, BookmarkableType entityType, Long entityId);
    Optional<Bookmark> findByPublicIdAndUserId(UUID publicId, Long userId);
    long countByEntityTypeAndEntityId(BookmarkableType entityType, Long entityId);
}
