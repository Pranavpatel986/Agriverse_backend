package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.PathogenType;
import com.agriverse.api.reference.entity.PlantDisease;
import com.agriverse.api.reference.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PlantDiseaseRepository extends JpaRepository<PlantDisease, Long> {
    Optional<PlantDisease> findByPublicId(UUID publicId);

    @Query("select d from PlantDisease d where (:pathogenType is null or d.pathogenType = :pathogenType) " +
            "and (:severity is null or d.severity = :severity) " +
            "and (:cropId is null or exists (select 1 from PlantDiseaseCrop pdc where pdc.plantDisease = d and pdc.crop.id = :cropId))")
    Page<PlantDisease> search(@Param("pathogenType") PathogenType pathogenType,
                               @Param("severity") Severity severity,
                               @Param("cropId") Long cropId,
                               Pageable pageable);
}
