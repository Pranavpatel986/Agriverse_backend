package com.agriverse.api.admin.controller;

import com.agriverse.api.admin.service.AdminUserService;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.identity.dto.AdminUserSummaryResponse;
import com.agriverse.api.identity.dto.CreateUserRequest;
import com.agriverse.api.identity.dto.UpdateUserRoleRequest;
import com.agriverse.api.identity.dto.UpdateUserStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Users — REST API Specification, Section 12. ADMIN-only. */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public PageResponse<AdminUserSummaryResponse> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return adminUserService.list(role, status, pageable);
    }

    @PostMapping
    public ResponseEntity<AdminUserSummaryResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.create(request));
    }

    @PatchMapping("/{id}/role")
    public AdminUserSummaryResponse updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return adminUserService.updateRole(id, request);
    }

    @PatchMapping("/{id}/status")
    public AdminUserSummaryResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminUserService.updateStatus(id, request);
    }
}
