package com.finhub.investment.repository;

import com.finhub.investment.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    @Query("SELECT h FROM Holding h JOIN FETCH h.stock WHERE h.portfolio.id = :portfolioId")
    List<Holding> findByPortfolioIdWithStock(@Param("portfolioId") Long portfolioId);

    Optional<Holding> findByPortfolioIdAndStockId(Long portfolioId, Long stockId);
}
