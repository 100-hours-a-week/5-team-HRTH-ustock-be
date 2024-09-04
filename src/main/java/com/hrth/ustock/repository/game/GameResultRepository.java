package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameNews;
import com.hrth.ustock.entity.game.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {

}
