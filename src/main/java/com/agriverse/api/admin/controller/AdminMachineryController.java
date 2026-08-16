package com.agriverse.api.admin.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.service.MachineryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Machinery management — REST API Specification, Section 12. EDITOR/ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/machinery")
@RequiredArgsConstructor
public class AdminMachineryController {

    private final MachineryService machineryService;

    @PostMapping
    public ResponseEntity<IdCreatedResponse> create(@Valid @RequestBody CreateMachineryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(machineryService.create(request));
    }

    @PutMapping("/{id}")
    public MachineryDetailResponse update(@PathVariable UUID id, @RequestBody UpdateMachineryRequest request) {
        return machineryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        machineryService.delete(id);
        return new MessageResponse("Machinery entry deleted");
    }
}
