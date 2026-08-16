package com.agriverse.api.learning.dto;

import java.util.List;
import java.util.UUID;

public record QuizQuestionPublicResponse(UUID id, String questionText, List<QuizOptionPublicResponse> options) {
}
