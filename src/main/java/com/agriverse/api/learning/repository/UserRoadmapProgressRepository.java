package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.UserRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoadmapProgressRepository extends JpaRepository<UserRoadmapProgress, Long> {
    Optional<UserRoadmapProgress> findByUserIdAndRoadmapId(Long userId, Long roadmapId);
}
