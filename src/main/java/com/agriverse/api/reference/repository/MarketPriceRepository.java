package com.agriverse.api.reference.repository;

import com.agriverse.api.reference.entity.MarketPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    @Query("select mp from MarketPrice mp where (:cropId is null or mp.crop.id = :cropId) " +
            "and (:state is null or mp.state = :state) order by mp.priceDate desc")
    Page<MarketPrice> search(@Param("cropId") Long cropId, @Param("state") String state, Pageable pageable);
}
