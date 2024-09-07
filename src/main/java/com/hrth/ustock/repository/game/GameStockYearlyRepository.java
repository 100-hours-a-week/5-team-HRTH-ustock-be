package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameStockYearly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GameStockYearlyRepository extends JpaRepository<GameStockYearly, Long> {

    Optional<GameStockYearly> findByGameStockInfoId(Long id);

    @Query(value = "select y.price from GameStockYearly y " +
            "left join GameStockInfo i on y.gameStockInfo.id = i.id " +
            "where y.gameStockInfo.id = :id and y.year = :year")
    Integer findPriceByGameStockInfoIdAndYear(Long id, int year);
}
