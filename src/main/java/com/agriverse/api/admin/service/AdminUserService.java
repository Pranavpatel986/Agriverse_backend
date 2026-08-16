package com.agriverse.api.admin.service;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.identity.dto.AdminUserSummaryResponse;
import com.agriverse.api.identity.dto.CreateUserRequest;
import com.agriverse.api.identity.dto.UpdateUserRoleRequest;
import com.agriverse.api.identity.dto.UpdateUserStatusRequest;
import com.agriverse.api.identity.entity.Role;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.entity.UserStatus;
import com.agriverse.api.identity.repository.RoleRepository;
import com.agriverse.api.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Backs the Admin > Users endpoints (REST API Specification, Section 12). ADMIN-only. */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> list(String role, String status, Pageable pageable) {
        UserStatus parsedStatus = status != null ? parseStatus(status) : null;
        Page<User> page = userRepository.search(role, parsedStatus, pageable);
        return PageResponse.of(page, page.getContent().stream().map(this::toSummary).toList());
    }

    @Transactional
    public AdminUserSummaryResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }
        Role role = roleRepository.findByName(request.role().trim().toUpperCase())
                .orElseThrow(() -> new BadRequestException("role must be one of: READER, AUTHOR, EDITOR, ADMIN"));

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return toSummary(user);
    }

    @Transactional
    public AdminUserSummaryResponse updateRole(UUID id, UpdateUserRoleRequest request) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        Role newRole = roleRepository.findByName(request.role().trim().toUpperCase())
                .orElseThrow(() -> new BadRequestException("role must be one of: READER, AUTHOR, EDITOR, ADMIN"));

        boolean demotingFromAdmin = Role.ADMIN.equals(user.getRole().getName()) && !Role.ADMIN.equals(newRole.getName());
        if (demotingFromAdmin) {
            long remainingAdmins = userRepository.search(Role.ADMIN, null, Pageable.unpaged()).getTotalElements();
            if (remainingAdmins <= 1) {
                throw new ConflictException("Cannot remove the last remaining Admin");
            }
        }

        user.setRole(newRole);
        userRepository.save(user);
        return toSummary(user);
    }

    @Transactional
    public AdminUserSummaryResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        String value = request.status().trim().toLowerCase();
        if (!value.equals("active") && !value.equals("suspended")) {
            throw new BadRequestException("status must be 'active' or 'suspended'");
        }
        user.setStatus(value.equals("active") ? UserStatus.ACTIVE : UserStatus.SUSPENDED);
        userRepository.save(user);
        return toSummary(user);
    }

    private UserStatus parseStatus(String value) {
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: active, suspended, pending_verification, deleted");
        }
    }

    private AdminUserSummaryResponse toSummary(User u) {
        return new AdminUserSummaryResponse(u.getPublicId(), u.getFullName(), u.getEmail(), u.getRole().getName(),
                u.getStatus().name().toLowerCase(), u.getCreatedAt(), u.getLastLoginAt());
    }
}
