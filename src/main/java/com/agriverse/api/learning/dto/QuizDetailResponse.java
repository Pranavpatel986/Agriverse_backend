package com.agriverse.api.learning.dto;

import java.util.List;
import java.util.UUID;

public record QuizDetailResponse(UUID id, String title, int passingScore, List<QuizQuestionPublicResponse> questions) {
}
