package com.agriverse.api.ai.dto;

import java.util.UUID;

/** One retrieved-and-cited article backing an assistant answer. */
public record SourceReference(UUID articleId, String title, String slug) {
}
