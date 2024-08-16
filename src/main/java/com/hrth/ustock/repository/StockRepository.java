package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
}
