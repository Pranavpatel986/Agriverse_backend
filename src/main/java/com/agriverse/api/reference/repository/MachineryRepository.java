package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.Machinery;
import com.agriverse.api.reference.entity.MachineryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    Optional<Machinery> findByPublicId(UUID publicId);

    @Query("select m from Machinery m where (:category is null or m.category = :category) " +
            "and (:cropId is null or m.applicableCrop.id = :cropId)")
    Page<Machinery> search(@Param("category") MachineryCategory category, @Param("cropId") Long cropId, Pageable pageable);
}
