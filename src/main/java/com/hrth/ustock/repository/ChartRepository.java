package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartRepository extends JpaRepository<Chart, Long> {

    // 과거~현재 오름차순
    List<Chart> findAllByTimeBetweenOrderByTimeAsc(Long start, Long end);

    // 제일 최신 데이터
    List<Chart> findTop2ByStockCodeOrderByIdDesc(String stockCode);
}
