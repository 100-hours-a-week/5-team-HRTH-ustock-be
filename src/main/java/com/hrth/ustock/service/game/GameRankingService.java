package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.rank.GameRankingDto;
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
        return gameResultRepository.findTop10ByOrderByBudgetDescIdAsc().stream()
                .map(GameResult::toDto)
                .toList();
    }

}
