package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChartRepository extends JpaRepository<Chart, Long> {

    // 과거~현재 오름차순
    List<Chart> findAllByStockCodeAndDateBetween(String code, String start, String end);
}
