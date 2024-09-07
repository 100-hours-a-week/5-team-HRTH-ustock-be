package com.hrth.ustock.controller.game;

import com.hrth.ustock.controller.api.GameApi;
import com.hrth.ustock.dto.game.GameInitResponseDto;
import com.hrth.ustock.dto.game.hint.GameHintRequestDto;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.news.GameNewsResponseDto;
import com.hrth.ustock.dto.game.rank.GameRankingDto;
import com.hrth.ustock.dto.game.result.GameInterimResponseDto;
import com.hrth.ustock.dto.game.result.GameResultHoldingResponseDto;
import com.hrth.ustock.dto.game.result.GameResultResponseDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.game.GamePlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.hrth.ustock.exception.domain.game.GameExceptionType.INVALID_YEAR_INPUT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/game")
public class GameController implements GameApi {

    private final GamePlayService gamePlayService;
    private final CustomUserService customUserService;

    @GetMapping("/start")
    public ResponseEntity<GameInitResponseDto> startGame(@RequestParam String nickname) {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.startGame(userId, nickname);

        return ResponseEntity.ok(new GameInitResponseDto());
    }

    @GetMapping("/stock")
    public ResponseEntity<List<GameStockInfoResponseDto>> showStockList(@RequestParam int year, @RequestParam long gameId) {
        if (2014 <= year && year <= 2023)
            throw new GameException(INVALID_YEAR_INPUT);

        List<GameStockInfoResponseDto> stockList = gamePlayService.showStockList(year, gameId);

        return ResponseEntity.ok(stockList);
    }

    @PostMapping("/stock")
    public ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto) {

        return ResponseEntity.ok().build();
    }

    @GetMapping("/interim")
    public ResponseEntity<GameInterimResponseDto> showInterimResult(@RequestParam Long gameId) {

        return ResponseEntity.ok(new GameInterimResponseDto());
    }

    @GetMapping("/hint")
    public ResponseEntity<GameHintResponseDto> showHint(@RequestBody GameHintRequestDto requestDto) {

        return ResponseEntity.ok(new GameHintResponseDto());
    }

    @GetMapping("/result")
    public ResponseEntity<List<GameResultResponseDto>> showResult(@RequestParam long gameId) {

        List<GameResultResponseDto> resultList = new ArrayList<>();

        return ResponseEntity.ok(resultList);
    }

    @GetMapping("/result/holding")
    public ResponseEntity<List<GameResultHoldingResponseDto>> showResultHolding(@RequestParam long gameId) {

        List<GameResultHoldingResponseDto> holdingList = new ArrayList<>();

        return ResponseEntity.ok(holdingList);
    }

    @GetMapping("/news")
    public ResponseEntity<List<GameNewsResponseDto>> showHoldingNews(@RequestParam long stockId) {

        List<GameNewsResponseDto> newsList = new ArrayList<>();

        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<GameRankingDto>> showRanking() {

        List<GameRankingDto> ranking = new ArrayList<>();

        return ResponseEntity.ok(ranking);
    }
}
