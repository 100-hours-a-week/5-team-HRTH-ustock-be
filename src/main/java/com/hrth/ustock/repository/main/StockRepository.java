package com.hrth.ustock.repository.main;

import com.hrth.ustock.entity.main.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {

    Optional<Stock> findByCode(String code);

    List<Stock> findAllByCodeIn(List<String> codes);

    @Query("SELECT s FROM Stock s WHERE s.name LIKE :name% ORDER BY s.name ASC")
    List<Stock> findByNameStartingWith(@Param("name") String name);

    @Query("SELECT s FROM Stock s WHERE s.name LIKE %:name% AND s.name NOT LIKE :name% ORDER BY s.name ASC")
    List<Stock> findByNameContainingButNotStartingWith(@Param("name") String name);

    @Query("SELECT s FROM Stock s WHERE s.code LIKE :code% ORDER BY s.code ASC")
    List<Stock> findByCodeStartingWith(@Param("code") String code);

    @Query("SELECT s FROM Stock s WHERE s.code LIKE %:code% AND s.code NOT LIKE :code% ORDER BY s.code ASC")
    List<Stock> findByCodeContainingButNotStartingWith(@Param("code") String code);
}
