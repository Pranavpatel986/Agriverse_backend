package com.agriverse.api.identity.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.common.dto.SimplePageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.identity.dto.*;
import com.agriverse.api.identity.service.UserService;
import com.agriverse.api.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Users endpoint group — REST API Specification, Section 2. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PaginationProperties paginationProperties;

    @GetMapping("/me")
    public UserProfileResponse getCurrentProfile() {
        return userService.getCurrentProfile(SecurityUtils.currentUserId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateCurrentProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateCurrentProfile(SecurityUtils.currentUserId(), request);
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(SecurityUtils.currentUserId(), request);
        return new MessageResponse("Password updated successfully");
    }

    @DeleteMapping("/me")
    public MessageResponse deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        userService.deleteAccount(SecurityUtils.currentUserId(), request);
        return new MessageResponse("Account scheduled for deletion");
    }

    @GetMapping("/{publicId}")
    public PublicUserProfileResponse getPublicProfile(@PathVariable UUID publicId) {
        return userService.getPublicProfile(publicId);
    }

    @GetMapping("/me/reading-history")
    public SimplePageResponse<ReadingHistoryItemResponse> getReadingHistory(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return userService.getReadingHistory(SecurityUtils.currentUserId(), pageable);
    }
}
