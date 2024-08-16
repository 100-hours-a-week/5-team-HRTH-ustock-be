package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findHoldingByPortfolioIdAndStockCode(Long pfid, String code);
}
