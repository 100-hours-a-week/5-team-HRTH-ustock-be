package com.hrth.ustock.repository.main;

import com.hrth.ustock.entity.main.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartRepository extends JpaRepository<Chart, Long> {

    // 과거~현재 오름차순
    List<Chart> findAllByStockCodeAndDateBetween(String code, String start, String end);
}
