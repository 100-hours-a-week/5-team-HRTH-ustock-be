package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameHint;
import com.hrth.ustock.entity.game.HintLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.Optional;

public interface GameHintRepository extends JpaRepository<GameHint, Long> {
    Optional<GameHint> findByGameStockYearlyIdAndLevel(Long id, HintLevel level);
    
    @Query(value = "select h from GameHint h " +
            "join fetch h.gameStockYearly y " +
            "where y.gameStockInfo.id in (:stockIds) " +
            "and y.year = :year")
    List<GameHint> findByStockIds(List<Long> stockIds, int year);
}
