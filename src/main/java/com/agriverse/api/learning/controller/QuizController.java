package com.agriverse.api.learning.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.learning.dto.*;
import com.agriverse.api.learning.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Quizzes endpoint group — REST API Specification, Section 10. */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final PaginationProperties paginationProperties;

    @GetMapping("/{id}")
    public QuizDetailResponse getById(@PathVariable UUID id) {
        return quizService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    public ResponseEntity<QuizCreatedResponse> create(@Valid @RequestBody CreateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.create(request));
    }

    @PostMapping("/{id}/attempts")
    public QuizAttemptResultResponse submitAttempt(@PathVariable UUID id, @Valid @RequestBody SubmitQuizAttemptRequest request) {
        return quizService.submitAttempt(id, request);
    }

    @GetMapping("/{id}/attempts/me")
    public ContentListResponse<QuizAttemptHistoryItemResponse> myAttempts(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return quizService.myAttempts(id, pageable);
    }
}
