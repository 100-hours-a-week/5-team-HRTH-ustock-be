package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    public List<GameResult> findTop10ByOrderByBudgetDescIdAsc();
}
