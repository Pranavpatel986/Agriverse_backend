package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    Optional<Roadmap> findBySlug(String slug);
    Optional<Roadmap> findByPublicId(UUID publicId);
    boolean existsBySlug(String slug);
}
