package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {

    Optional<Stock> findByCode(String code);

    List<Stock> findAllByNameContaining(String name);
}
