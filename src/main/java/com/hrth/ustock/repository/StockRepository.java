package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {

    Optional<Stock> findByCode(String code);

    @Query("SELECT s FROM Stock s WHERE s.name LIKE :name% ORDER BY s.name ASC")
    List<Stock> findByNameStartingWith(@Param("name") String name);

    @Query("SELECT s FROM Stock s WHERE s.name LIKE %:name% AND s.name NOT LIKE :name% ORDER BY s.name ASC")
    List<Stock> findByNameContainingButNotStartingWith(@Param("name") String name);
}
