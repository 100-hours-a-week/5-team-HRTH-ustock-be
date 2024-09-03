package com.hrth.ustock.controller.game;

import com.hrth.ustock.dto.game.*;
import com.hrth.ustock.entity.game.HintLevel;
import com.hrth.ustock.service.auth.CustomUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/game")
@Tag(name = "Skrrr Game", description = "Skrrr 게임 관련 API")
public class GameController {

    private final CustomUserService customUserService;

    @GetMapping("/start")
    @Operation(summary = "게임 정보 초기화", description = "게임을 시작할 때 반드시 호출")
    public ResponseEntity<GameInitResponseDto> startGame(@RequestParam String nickname) {
        Long userId = customUserService.getCurrentUserDetails().getUserId();

        return ResponseEntity.ok(new GameInitResponseDto());
    }

    @GetMapping("/stock")
    @Operation(summary = "종목 리스트 조회", description = "년도를 기반으로 종목 리스트를 조회, 첫 해에는 change, changeRate가 null")
    public ResponseEntity<List<GameStockInfoResponseDto>> showStockList(@RequestParam int year) {

        List<GameStockInfoResponseDto> stockList = new ArrayList<>();

        return ResponseEntity.ok(stockList);
    }

    @PostMapping("/stock")
    @Operation(summary = "종목 거래 요청",
            description = "종목 아이디, 수량, 매도/매수 여부를 바탕으로 거래를 진행 (매도: SELL, 매수: BUY)")
    public ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto) {

        return ResponseEntity.ok().build();
    }

    @GetMapping("/interim")
    @Operation(summary = "회차별 결과 조회",
            description = "중간 결과를 순위대로 반환, 플레이어일 경우 보유 종목도 반환하지만 AI는 반환하지 않음 (사용자: USER, AI: COM)")
    public ResponseEntity<GameInterimResponseDto> showHolding(@RequestParam Long gameId) {

        return ResponseEntity.ok(new GameInterimResponseDto());
    }

    @GetMapping("/hint")
    @Operation(summary = "정보 거래소 힌트 조회",
            description = "년도, 종목 아이디, 레벨을 받으면 힌트를 반환, 레벨은 반드시 ONE, TWO, THREE 중 하나를 사용해주세요.")
    public ResponseEntity<GameHintResponseDto> showHint(@RequestBody GameHintRequestDto requestDto) {

        return ResponseEntity.ok(new GameHintResponseDto());
    }

    @GetMapping("/result")
    @Operation(summary = "최종 결과 조회", description = "최종 결과를 순위대로 반환 (사용자: USER, AI: COM)")
    public ResponseEntity<List<GameResultResponseDto>> showResult(@RequestParam long gameId) {

        List<GameResultResponseDto> resultList = new ArrayList<>();

        return ResponseEntity.ok(resultList);
    }

    @GetMapping("/result/holding")
    @Operation(summary = "게임 내 종목 리스트 조회",
            description = "게임에 사용된 종목의 리스트를 반환, 뉴스는 /v1/news/{code}, 차트는 /v1/stocks/{code}/chart를 이용해주세요. " +
                    "stockName은 원래의 종목명을 의미하며, inGame은 게임에서 사용된 종목명을 의미합니다.")
    public ResponseEntity<List<GameResultHoldingResponseDto>> showResultHolding(@RequestParam long gameId) {

        List<GameResultHoldingResponseDto> holdingList = new ArrayList<>();

        return ResponseEntity.ok(holdingList);
    }

    @GetMapping("/ranking")
    @Operation(summary = "게임 랭킹 리스트 조회", description = "게임의 최종 금액을 기준으로 내림차순 정렬된 리스트 반환 (10위까지)")
    public ResponseEntity<List<GameRankingDto>> showRanking() {

        List<GameRankingDto> ranking = new ArrayList<>();

        return ResponseEntity.ok(ranking);
    }
}
