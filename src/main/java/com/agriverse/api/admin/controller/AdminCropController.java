package com.agriverse.api.admin.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.reference.dto.CreateCropRequest;
import com.agriverse.api.reference.dto.CropResponse;
import com.agriverse.api.reference.dto.UpdateCropRequest;
import com.agriverse.api.reference.service.CropService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Crops management — same EDITOR/ADMIN pattern as Schemes/Diseases/Machinery. */
@RestController
@RequestMapping("/api/v1/admin/crops")
@RequiredArgsConstructor
public class AdminCropController {

    private final CropService cropService;

    @PostMapping
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CreateCropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.create(request));
    }

    @PutMapping("/{id}")
    public CropResponse update(@PathVariable UUID id, @RequestBody UpdateCropRequest request) {
        return cropService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        cropService.delete(id);
        return new MessageResponse("Crop deleted");
    }
}
