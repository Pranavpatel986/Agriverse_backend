package com.agriverse.api.content.repository;

import com.agriverse.api.content.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    List<ArticleTag> findByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
}
