package com.hrth.ustock.repository.main;

import com.hrth.ustock.entity.main.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findAllByUserUserId(Long userId);

    Optional<Holding> findHoldingByPortfolioIdAndStockCode(Long pfId, String code);
}
