package com.hrth.ustock.controller.game;

import com.hrth.ustock.controller.api.GameApi;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.rank.GameRankingDto;
import com.hrth.ustock.dto.game.result.GameInterimResponseDto;
import com.hrth.ustock.dto.game.result.GameResultResponseDto;
import com.hrth.ustock.dto.game.result.GameUserResponseDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.entity.game.HintLevel;
import com.hrth.ustock.service.game.GamePlayService;
import com.hrth.ustock.service.game.GameRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/game")
public class GameController implements GameApi {

    private final GamePlayService gamePlayService;
    private final GameRankingService gameRankingService;

    private static final long userId = 7L;

    @GetMapping("/start")
    public ResponseEntity<List<GameStockInfoResponseDto>> startGame(@RequestParam String nickname) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();
        return ResponseEntity.ok(gamePlayService.startGame(userId, nickname));
    }

    @GetMapping("/player")
    public ResponseEntity<List<GameUserResponseDto>> showPlayer() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getGamePlayerList(userId));
    }

    @GetMapping("/user")
    public ResponseEntity<GameUserResponseDto> showUser() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getUserInfo(userId));
    }

    @PostMapping("/stock")
    public ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.tradeHolding(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interim")
    public ResponseEntity<GameInterimResponseDto> showInterimResult() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getUserInterim(userId));
    }

    @GetMapping("/hint")
    public ResponseEntity<GameHintResponseDto> showHint(@RequestParam long stockId, @RequestParam HintLevel hintLevel) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        GameHintResponseDto hint = gamePlayService.getSingleHint(userId, stockId, hintLevel);

        return ResponseEntity.ok(hint);
    }

    @GetMapping("/result")
    public ResponseEntity<List<GameResultResponseDto>> showResult() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getGameResultList(userId));
    }

    @GetMapping("/result/stock")
    public ResponseEntity<List<GameStockInfoResponseDto>> showResultStockList() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<GameRankingDto>> showRanking() {

        List<GameRankingDto> ranking;
        ranking = gameRankingService.getRankingList();

        return ResponseEntity.ok(ranking);
    }

    @DeleteMapping("/stop")
    public ResponseEntity<?> stopGame() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();
        gamePlayService.deleteRedis(userId);

        return ResponseEntity.ok().build();
    }
}
