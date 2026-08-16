package com.agriverse.api.learning.dto;

import java.time.Instant;

public record QuizAttemptHistoryItemResponse(int score, int totalQuestions, boolean passed, Instant attemptedAt) {
}
