package com.agriverse.api.common.dto;

import java.util.List;

/** Envelope of the shape { "content": [...] } used by unpaginated list endpoints. */
public record ContentListResponse<T>(List<T> content) {
}
