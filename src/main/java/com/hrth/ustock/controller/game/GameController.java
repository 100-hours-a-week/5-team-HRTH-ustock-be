package com.hrth.ustock.controller.game;

import com.hrth.ustock.controller.api.GameApi;
import com.hrth.ustock.dto.game.GameInitResponseDto;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.news.GameNewsResponseDto;
import com.hrth.ustock.dto.game.rank.GameRankingDto;
import com.hrth.ustock.dto.game.redis.GameHoldingsInfo;
import com.hrth.ustock.dto.game.redis.GameUserInfo;
import com.hrth.ustock.dto.game.result.GamePlayerResponseDto;
import com.hrth.ustock.dto.game.result.GameResultResponseDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.entity.game.HintLevel;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.game.GameHintService;
import com.hrth.ustock.service.game.GameNewsService;
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
    private final GameNewsService gameNewsService;
    private final GameHintService gameHintService;
    private final GameRankingService gameRankingService;
    private final CustomUserService customUserService;

    private static final long userId = 7L;

    @GetMapping("/start")
    public ResponseEntity<GameInitResponseDto> startGame(@RequestParam String nickname) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.startGame(userId, nickname);

        // TODO: endpoint에서 dto를 return 할 필요가 없어보임
        // TODO: 이미 진행중인 게임이 있다면 해당 게임을 이어서 할지, 새로 시작할지 물어봐야 함
        // return ResponseEntity.ok(new GameInitResponseDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stock")
    public ResponseEntity<List<GameStockInfoResponseDto>> showStockList() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        List<GameStockInfoResponseDto> stockList = gamePlayService.showStockList(userId);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/users")
    public ResponseEntity<List<GameUserInfo>> showUserList() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getGameUserList(userId));
    }

    @GetMapping("/user")
    public ResponseEntity<GamePlayerResponseDto> showUser() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getPlayerInfo(userId));
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<GameHoldingsInfo>> showHoldings() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.showHoldingsList(userId));
    }


    @PostMapping("/stock")
    public ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        gamePlayService.tradeHolding(userId, requestDto.getStockId(), requestDto.getQuantity(), requestDto.getActing());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interim")
    public ResponseEntity<GamePlayerResponseDto> showInterimResult() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.getPlayerInterim(userId));
    }

    @GetMapping("/hint")
    public ResponseEntity<GameHintResponseDto> showHint(@RequestParam long stockId, @RequestParam HintLevel hintLevel) {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();
        log.info("Show hint for stockId: {}, hintLevel: {}", stockId, hintLevel);

        GameHintResponseDto hint = gamePlayService.getSingleHint(userId, stockId, hintLevel);

        return ResponseEntity.ok(hint);
    }

    // TODO: result, result/stock, result/news
    @GetMapping("/result")
    public ResponseEntity<List<GameResultResponseDto>> showResult() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();
        List<GameResultResponseDto> resultList = new ArrayList<>();

        return ResponseEntity.ok(resultList);
    }

    @GetMapping("/result/stock")
    public ResponseEntity<List<GameStockInfoResponseDto>> showResultStockList() {
//        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(gamePlayService.showStockList(userId));
    }

    @GetMapping("/result/news")
    public ResponseEntity<List<GameNewsResponseDto>> showStockNews(@RequestParam long stockId) {

        List<GameNewsResponseDto> newsList;
        newsList = gameNewsService.getStockNews(stockId);

        return ResponseEntity.ok(newsList);
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
