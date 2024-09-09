package com.hrth.ustock.controller.api;

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
import com.hrth.ustock.exception.common.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Skrrr Game", description = "Skrrr 게임 관련 API")
public interface GameApi {
    @Operation(
            summary = "게임 정보 초기화",
            description = "게임을 시작할 때 반드시 호출"
    )
    @Parameter(
            name = "nickname",
            description = "게임에서 사용할 임시 닉네임",
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<GameInitResponseDto> startGame(@RequestParam String nickname);

    @Operation(
            summary = "종목 리스트 조회",
            description = "년도를 기반으로 종목 리스트를 조회, 첫 해에는 change, changeRate가 null"
    )
    @Parameter(
            name = "year",
            description = "특정 연도 (2014~2023)",
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 날짜 양식입니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 종목을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서gameHintRepository버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<List<GameStockInfoResponseDto>> showStockList();

    @Operation(
            summary = "모든 유저 정보 조회 요청",
            description = "특정 연도의 플레이어(플레이어, ai) 정보를 가져옴"
    )
    ResponseEntity<List<GameUserInfo>> showUserList();

    @Operation(
            summary = "플레이어 정보 조회 요청",
            description = "특정 연도의 플레이어(플레이어) 정보를 가져옴"
    )
    ResponseEntity<GamePlayerResponseDto> showUser();

    @Operation(
            summary = "플레이어 보유 종목 조회",
            description = "특정 연도의 플레이어(플레이어) 보유 종목 정보를 가져옴"
    )
    ResponseEntity<List<GameHoldingsInfo>> showHoldings();

    @Operation(
            summary = "종목 거래 요청",
            description = "종목 아이디, 수량, 매도/매수 여부를 바탕으로 거래를 진행 (매도: SELL, 매수: BUY)"
    )
    ResponseEntity<Void> tradeStock(@RequestBody GameTradeRequestDto requestDto);

    @Operation(
            summary = "회차별 결과 조회",
            description = "중간 결과를 순위대로 반환, 플레이어일 경우 보유 종목도 반환하지만 AI는 반환하지 않음 (사용자: USER, AI: COM)"
    )
    ResponseEntity<GamePlayerResponseDto> showInterimResult();

    @Operation(
            summary = "정보 거래소 힌트 조회",
            description = "년도, 종목 아이디, 레벨을 받으면 힌트를 반환, 레벨은 반드시 ONE, TWO, THREE 중 하나를 사용해주세요."
    )
    ResponseEntity<GameHintResponseDto> showHint(@RequestParam long stockId, @RequestParam HintLevel hintLevel);

    @Operation(
            summary = "최종 결과 조회",
            description = "최종 결과를 순위대로 반환 (사용자: USER, AI: COM)"
    )
    @Parameter(
            name = "gameId",
            description = "게임 아이디",
            required = true
    )
    ResponseEntity<List<GameResultResponseDto>> showResult();

    @Operation(
            summary = "게임 내 종목 리스트 조회",
            description = "게임에 사용된 종목의 리스트를 반환, 차트는 /v1/stocks/{code}/chart를 이용해주세요. " +
                    "stockName은 원래의 종목명을 의미하며, inGame은 게임에서 사용된 종목명을 의미합니다."
    )
    @Parameter(
            name = "gameId",
            description = "게임 아이디",
            required = true
    )
    ResponseEntity<List<GameStockInfoResponseDto>> showResultStockList();

    @Operation(
            summary = "게임 종목별 뉴스 조회",
            description = "종목 아이디를 바탕으로 뉴스 리스트를 반환"
    )
    @Parameter(
            name = "stockId",
            description = "게임 종목 아이디",
            required = true
    )
    ResponseEntity<List<GameNewsResponseDto>> showStockNews(@RequestParam long stockId);

    @Operation(
            summary = "게임 랭킹 리스트 조회",
            description = "게임의 최종 금액을 기준으로 내림차순 정렬된 리스트 반환 (10위까지)"
    )
    ResponseEntity<List<GameRankingDto>> showRanking();

    @Operation(
            summary = "userId=7의 진행중인 게임 삭제(redis)",
            description = "개발용, userId는 7로 고정"
    )
    ResponseEntity<?> stopGame();
}
