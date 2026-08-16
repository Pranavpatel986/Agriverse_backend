package com.agriverse.api.content.dto;

import java.util.List;
import java.util.UUID;

public record CategoryDetailResponse(UUID id, String name, String description, UUID parentCategoryId,
                                      long articleCount, List<CategoryTreeNode> children) {
}
