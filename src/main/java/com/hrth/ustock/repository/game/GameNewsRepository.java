package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameNews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameNewsRepository extends JpaRepository<GameNews, Long> {
    List<GameNews> findAllByGameStockYearlyIdIn(List<Long> ids);
}
