package com.agriverse.api.admin.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.service.GovernmentSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Government Schemes management — REST API Specification, Section 12. EDITOR/ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/schemes")
@RequiredArgsConstructor
public class AdminSchemeController {

    private final GovernmentSchemeService schemeService;

    @PostMapping
    public ResponseEntity<IdCreatedResponse> create(@Valid @RequestBody CreateSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schemeService.create(request));
    }

    @PutMapping("/{id}")
    public SchemeDetailResponse update(@PathVariable UUID id, @RequestBody UpdateSchemeRequest request) {
        return schemeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        schemeService.delete(id);
        return new MessageResponse("Scheme deleted");
    }
}
