package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.hint.GameHintRequestDto;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.entity.game.GameHint;
import com.hrth.ustock.entity.game.GameStockYearly;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.exception.domain.game.GameExceptionType;
import com.hrth.ustock.repository.game.GameHintRepository;
import com.hrth.ustock.repository.game.GameStockYearlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hrth.ustock.exception.domain.game.GameExceptionType.*;

@Service
@RequiredArgsConstructor
public class GameHintService {
    private final GameHintRepository gameHintRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;

    public GameHintResponseDto getSingleHint(GameHintRequestDto gameHintRequestDto) {
        long stockId = gameHintRequestDto.getStockId();
        int year = gameHintRequestDto.getYear();

        GameStockYearly yearInfo = gameStockYearlyRepository.findByGameStockInfoIdAndYear(stockId, year)
                .orElseThrow(() -> new GameException(YEAR_INFO_NOT_FOUND));

        return gameHintRepository.findByGameStockYearlyIdAndLevel(yearInfo.getId(), gameHintRequestDto.getLevel())
                .map(GameHint::toDto)
                .orElseThrow(() -> new GameException(HINT_NOT_FOUND));
    }
}
