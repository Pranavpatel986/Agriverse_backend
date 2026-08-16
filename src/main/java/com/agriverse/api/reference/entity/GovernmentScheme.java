package com.agriverse.api.reference.entity;

import com.agriverse.api.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** A directory entry for a government support scheme, searchable/filterable by state and crop. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "government_schemes")
public class GovernmentScheme extends AuditableEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_type", nullable = false, length = 20)
    private BeneficiaryType beneficiaryType;

    @Column(length = 100)
    private String state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Column(name = "benefit_summary", nullable = false, columnDefinition = "TEXT")
    private String benefitSummary;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "official_url", nullable = false, length = 500)
    private String officialUrl;

    @Column(nullable = false, length = 255)
    private String source;

    @Column(name = "last_verified_at", nullable = false)
    private Instant lastVerifiedAt;
}
