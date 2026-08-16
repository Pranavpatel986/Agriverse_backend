package com.agriverse.api.analytics.repository;

import com.agriverse.api.analytics.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("select sh.queryText as term, count(sh) as cnt from SearchHistory sh where sh.searchedAt >= :since " +
            "group by sh.queryText order by count(sh) desc")
    List<TrendingTermProjection> findTrending(@Param("since") Instant since, org.springframework.data.domain.Pageable pageable);

    interface TrendingTermProjection {
        String getTerm();
        long getCnt();
    }
}
