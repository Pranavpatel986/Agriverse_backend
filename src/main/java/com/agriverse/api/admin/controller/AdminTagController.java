package com.agriverse.api.admin.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.content.dto.CreateTagRequest;
import com.agriverse.api.content.dto.TagResponse;
import com.agriverse.api.content.dto.UpdateTagRequest;
import com.agriverse.api.content.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Tags management — EDITOR/ADMIN, same coarse gate as other admin reference-data controllers. */
@RestController
@RequestMapping("/api/v1/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(request));
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTagRequest request) {
        return tagService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        tagService.delete(id);
        return new MessageResponse("Tag deleted");
    }
}
