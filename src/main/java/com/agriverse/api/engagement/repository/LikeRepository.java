package com.agriverse.api.engagement.repository;

import com.agriverse.api.engagement.entity.Like;
import com.agriverse.api.engagement.entity.LikeableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndEntityTypeAndEntityId(Long userId, LikeableType entityType, Long entityId);
    long countByEntityTypeAndEntityId(LikeableType entityType, Long entityId);
}
