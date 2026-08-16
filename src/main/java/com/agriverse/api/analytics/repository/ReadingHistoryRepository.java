package com.agriverse.api.analytics.repository;

import com.agriverse.api.analytics.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    Page<ReadingHistory> findByUserIdOrderByReadAtDesc(Long userId, Pageable pageable);

    @Query("select rh.article.id from ReadingHistory rh where rh.user.id = :userId group by rh.article.id " +
            "order by max(rh.readAt) desc")
    List<Long> findRecentArticleIdsByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("select rh.article.category.id from ReadingHistory rh where rh.user.id = :userId " +
            "group by rh.article.category.id order by count(rh) desc")
    List<Long> findTopCategoryIdsByUser(@Param("userId") Long userId, Pageable pageable);
}
