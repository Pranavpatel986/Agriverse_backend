package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CropRepository extends JpaRepository<Crop, Long> {
    Optional<Crop> findByPublicId(UUID publicId);
    Optional<Crop> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    @Query("select c from Crop c where :q is null or lower(c.name) like lower(concat('%', :q, '%'))")
    Page<Crop> search(@Param("q") String q, Pageable pageable);
}
