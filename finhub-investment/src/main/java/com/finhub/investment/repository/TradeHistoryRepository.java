package com.finhub.investment.repository;

import com.finhub.investment.domain.TradeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    @Query("SELECT t FROM TradeHistory t JOIN FETCH t.stock WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Page<TradeHistory> findByUserIdWithStock(@Param("userId") Long userId, Pageable pageable);
}
