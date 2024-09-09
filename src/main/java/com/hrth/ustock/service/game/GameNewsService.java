package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.news.GameNewsResponseDto;
import com.hrth.ustock.entity.game.GameNews;
import com.hrth.ustock.entity.game.GameStockYearly;
import com.hrth.ustock.repository.game.GameNewsRepository;
import com.hrth.ustock.repository.game.GameStockYearlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameNewsService {
    private final GameStockYearlyRepository gameStockYearlyRepository;
    private final GameNewsRepository gameNewsRepository;

    public List<GameNewsResponseDto> getStockNews(long stockId) {
        List<Long> yearlyStockIds = gameStockYearlyRepository.findAllByGameStockInfoId(stockId).stream()
                .map(GameStockYearly::getId)
                .toList();
        return gameNewsRepository.findAllByGameStockYearlyIdIn(yearlyStockIds).stream()
                .map(GameNews::toDto)
                .toList();
    }

}
