package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
    List<RoadmapStep> findByRoadmapIdOrderByStepOrderAsc(Long roadmapId);
    long countByRoadmapId(Long roadmapId);
    Optional<RoadmapStep> findByPublicId(UUID publicId);
}
