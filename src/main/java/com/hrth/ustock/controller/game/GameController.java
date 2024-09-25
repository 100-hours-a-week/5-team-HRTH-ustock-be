package com.hrth.ustock.controller.game;

import com.hrth.ustock.controller.api.GameApi;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.interim.GameInterimResponseDto;
import com.hrth.ustock.dto.game.result.GameRankingDto;
import com.hrth.ustock.dto.game.result.GameResultResponseDto;
import com.hrth.ustock.dto.game.result.GameResultStockDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.dto.game.user.GameUserResponseDto;
import com.hrth.ustock.entity.game.HintLevel;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.game.GamePlayService;
import com.hrth.ustock.service.game.GameRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/game")
public class GameController implements GameApi {

    private final GamePlayService gamePlayService;
    private final GameRankingService gameRankingService;
    private final CustomUserService customUserService;

    @GetMapping("/start")
    public ResponseEntity<List<GameStockInfoResponseDto>> startGame(@RequestParam String nickname) {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.startGame(userId, nickname));
    }

//    @GetMapping("/player")
//    public ResponseEntity<List<GameUserResponseDto>> showPlayer() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();
//
//        return ResponseEntity.ok(gamePlayService.getGamePlayerList(userId));
//    }

    @GetMapping("/user")
    public ResponseEntity<GameUserResponseDto> showUser() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getUserInfo(userId));
    }

    @PostMapping("/stock")
    public ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto) {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.tradeHolding(userId, requestDto, 0);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interim")
    public ResponseEntity<GameInterimResponseDto> showInterimResult() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getUserInterim(userId));
    }

    @GetMapping("/hint")
    public ResponseEntity<GameHintResponseDto> showHint(@RequestParam long stockId, @RequestParam HintLevel hintLevel) {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        GameHintResponseDto hint = gamePlayService.getSingleHint(userId, stockId, hintLevel);

        return ResponseEntity.ok(hint);
    }

    @GetMapping("/result")
    public ResponseEntity<List<GameResultResponseDto>> showResult() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getGameResultList(userId));
    }

    @GetMapping("/result/stock")
    public ResponseEntity<List<GameResultStockDto>> showResultStockList() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getGameResultStock(userId));
    }

    @PostMapping("/result/save")
    public ResponseEntity<?> saveRanking() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.saveRankInfo(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<GameRankingDto>> showRanking() {

        List<GameRankingDto> ranking;
        ranking = gameRankingService.getRankingList();

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/stop")
    public ResponseEntity<?> stopGame() {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.deleteRedis(userId);

        return ResponseEntity.ok().build();
    }
}
