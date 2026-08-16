package com.agriverse.api.learning.dto;

public record QuizAttemptResultResponse(int score, int totalQuestions, boolean passed) {
}
