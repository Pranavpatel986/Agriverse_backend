package com.agriverse.api.reference.entity;

import com.agriverse.api.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Junction table resolving the Plant_Diseases <-> Crops N:M relationship. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plant_disease_crops", uniqueConstraints = @UniqueConstraint(columnNames = {"plant_disease_id", "crop_id"}))
public class PlantDiseaseCrop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_disease_id", nullable = false)
    private PlantDisease plantDisease;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;
}
