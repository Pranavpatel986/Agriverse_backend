package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.BeneficiaryType;
import com.agriverse.api.reference.entity.GovernmentScheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GovernmentSchemeRepository extends JpaRepository<GovernmentScheme, Long> {
    Optional<GovernmentScheme> findByPublicId(UUID publicId);

    @Query("select s from GovernmentScheme s where (:state is null or s.state = :state) " +
            "and (:cropId is null or s.crop.id = :cropId) " +
            "and (:beneficiaryType is null or s.beneficiaryType = :beneficiaryType)")
    Page<GovernmentScheme> search(@Param("state") String state,
                                   @Param("cropId") Long cropId,
                                   @Param("beneficiaryType") BeneficiaryType beneficiaryType,
                                   Pageable pageable);
}
