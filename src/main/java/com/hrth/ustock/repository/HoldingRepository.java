package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findAllByUserUserId(Long userId);
    Optional<Holding> findHoldingByPortfolioIdAndStockCode(Long pfId, String code);
}
