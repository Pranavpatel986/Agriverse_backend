package com.agriverse.api.content.repository;

import com.agriverse.api.content.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByPublicId(UUID publicId);
    List<Tag> findByPublicIdIn(Set<UUID> publicIds);
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCase(String name);
    List<Tag> findAllByOrderByNameAsc();
}
