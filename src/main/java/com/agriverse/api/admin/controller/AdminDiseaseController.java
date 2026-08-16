package com.agriverse.api.admin.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.service.PlantDiseaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Plant Diseases management — REST API Specification, Section 12. EDITOR/ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/diseases")
@RequiredArgsConstructor
public class AdminDiseaseController {

    private final PlantDiseaseService diseaseService;

    @PostMapping
    public ResponseEntity<IdCreatedResponse> create(@Valid @RequestBody CreateDiseaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diseaseService.create(request));
    }

    @PutMapping("/{id}")
    public DiseaseDetailResponse update(@PathVariable UUID id, @RequestBody UpdateDiseaseRequest request) {
        return diseaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        diseaseService.delete(id);
        return new MessageResponse("Disease entry deleted");
    }
}
