package com.agriverse.api.content.dto;

import java.util.List;
import java.util.UUID;

public record CategoryTreeNode(UUID id, String name, String slug, List<CategoryTreeNode> children) {
}
