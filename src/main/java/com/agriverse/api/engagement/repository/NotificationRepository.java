package com.agriverse.api.engagement.repository;

import com.agriverse.api.engagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Notification> findByPublicIdAndUserId(UUID publicId, Long userId);

    @Modifying
    @Query("update Notification n set n.read = true, n.readAt = :readAt where n.user.id = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId, @Param("readAt") java.time.Instant readAt);

    long countByUserIdAndReadFalse(Long userId);
}
