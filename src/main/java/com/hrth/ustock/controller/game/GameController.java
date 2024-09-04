package com.hrth.ustock.controller.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hrth.ustock.controller.adapter.GameApi;
import com.hrth.ustock.dto.game.*;
import com.hrth.ustock.dto.game.ranking.GameRankingDto;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.game.GamePlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/game")
public class GameController implements GameApi {

    private final GamePlayService gamePlayService;
    private final CustomUserService customUserService;

    @GetMapping("/start")
    public ResponseEntity<GameInitResponseDto> startGame(@RequestParam String nickname) throws JsonProcessingException {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.startGame(userId, nickname);

        return ResponseEntity.ok(new GameInitResponseDto());
    }

    @GetMapping("/stock")
    public ResponseEntity<List<GameStockInfoResponseDto>> showStockList(@RequestParam int year) {

        List<GameStockInfoResponseDto> stockList = new ArrayList<>();

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
