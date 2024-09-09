package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameHint;
import com.hrth.ustock.entity.game.HintLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameHintRepository extends JpaRepository<GameHint, Long> {
    Optional<GameHint> findByGameStockYearlyIdAndLevel(Long id, HintLevel level);
}
