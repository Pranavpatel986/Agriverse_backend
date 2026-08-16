package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.PlantDiseaseCrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantDiseaseCropRepository extends JpaRepository<PlantDiseaseCrop, Long> {
    List<PlantDiseaseCrop> findByPlantDiseaseId(Long plantDiseaseId);
    void deleteByPlantDiseaseId(Long plantDiseaseId);
}
