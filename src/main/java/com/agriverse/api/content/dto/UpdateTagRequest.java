package com.agriverse.api.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(@NotBlank @Size(min = 2, max = 60) String name) {
}
