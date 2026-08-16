package com.agriverse.api.content.entity;

/** Articles.status per DB spec: draft | in_review | published | archived. */
public enum ArticleStatus {
    DRAFT, IN_REVIEW, PUBLISHED, ARCHIVED;

    public static ArticleStatus fromWire(String value) {
        return ArticleStatus.valueOf(value.trim().toUpperCase());
    }

    public String toWire() {
        return name().toLowerCase();
    }
}
