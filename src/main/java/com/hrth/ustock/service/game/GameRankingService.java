package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.ranking.GameRankingDto;
import com.hrth.ustock.entity.game.GameResult;
import com.hrth.ustock.repository.game.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRankingService {
    private final GameResultRepository gameResultRepository;
    public List<GameRankingDto> getRankingList() {
        List<GameRankingDto> rankingList;
        rankingList = gameResultRepository.findTop10ByOrderByBudgetDescIdAsc().stream().map(GameResult::toDto).toList();
        return rankingList;
    }

}
