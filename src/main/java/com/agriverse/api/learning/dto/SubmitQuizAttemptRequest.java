package com.agriverse.api.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitQuizAttemptRequest(@NotEmpty @Valid List<SubmitAnswerRequest> answers) {
}
