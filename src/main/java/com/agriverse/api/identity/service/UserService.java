package com.agriverse.api.identity.service;

import com.agriverse.api.analytics.repository.ReadingHistoryRepository;
import com.agriverse.api.common.dto.SimplePageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.common.exception.UnauthorizedException;
import com.agriverse.api.identity.dto.*;
import com.agriverse.api.identity.entity.Author;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.entity.UserStatus;
import com.agriverse.api.identity.repository.AuthorRepository;
import com.agriverse.api.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Backs the Users endpoint group (REST API Specification, Section 2). */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile(Long userId) {
        User user = getActiveUser(userId);
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentProfile(Long userId, UpdateProfileRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        User user = getActiveUser(userId);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.locale() != null) {
            user.setLocale(request.locale());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getActiveUser(userId);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password incorrect");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BadRequestException("newPassword must differ from currentPassword");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = getActiveUser(userId);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Password incorrect");
        }
        user.setDeletedAt(Instant.now());
        user.setStatus(UserStatus.DELETED);
        // Anonymize PII per data-retention policy, preserving the row for FK integrity (comments, articles, etc.).
        user.setFullName("Deleted User");
        user.setEmail("deleted-" + user.getPublicId() + "@agriverse.invalid");
        user.setPhone(null);
        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PublicUserProfileResponse getPublicProfile(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicId));

        Author author = authorRepository.findByUserId(user.getId()).orElse(null);
        if (author != null) {
            return new PublicUserProfileResponse(user.getPublicId(), author.getDisplayName(), author.getBio(), author.getArticlesPublishedCount());
        }
        return new PublicUserProfileResponse(user.getPublicId(), user.getFullName(), null, null);
    }

    @Transactional(readOnly = true)
    public SimplePageResponse<ReadingHistoryItemResponse> getReadingHistory(Long userId, Pageable pageable) {
        var page = readingHistoryRepository.findByUserIdOrderByReadAtDesc(userId, pageable);
        var content = page.getContent().stream()
                .map(rh -> new ReadingHistoryItemResponse(
                        rh.getArticle().getPublicId(),
                        rh.getArticle().getTitle(),
                        rh.getArticle().getSlug(),
                        rh.getReadAt()))
                .toList();
        return new SimplePageResponse<>(content, page.getTotalElements(), page.getNumber());
    }

    private User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getPublicId(), user.getFullName(), user.getEmail(), user.getRole().getName(),
                user.getAvatarUrl(), user.getLocale(), user.getCreatedAt());
    }
}
