package com.hrth.ustock.repository;

import com.hrth.ustock.dto.chart.ChartResponseDTO;
import com.hrth.ustock.entity.portfolio.Chart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRepository extends JpaRepository<Chart, Long> {
    // 과거~현재 오름차순
    List<Chart> findAllByTimeBetweenOrderByTimeAsc(Long start, Long end);

//    // 일봉 차트 데이터와 뉴스 매핑
//    @Query("SELECT c.time, c.open, c.high, c.low, c.close, n.title, n.url " +
//            "FROM Chart c " +
//            "LEFT JOIN News n ON n.stock = c.stock AND n.time BETWEEN c.time AND " +
//            "(SELECT COALESCE(MIN(c2.time), c.time + 1) FROM Chart c2 WHERE c2.time > c.time) " +
//            "WHERE c.stock.code = :code " +
//            "ORDER BY c.time")
//    List<Object[]> findChartResponseDTO(@Param("code") String code);

}
