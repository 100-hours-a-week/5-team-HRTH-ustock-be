package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    // 종목별 시작시간~종료시간 뉴스 목록
    List<News> findAllByStockCodeAndDateBetween(String code, String start, String end);

    // 보유종목 리스트 최근 15개
    List<News> findTop15ByStockCodeOrderByDateDesc(String code);
}
