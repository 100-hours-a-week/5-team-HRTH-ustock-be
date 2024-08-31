package com.hrth.ustock.controller;

import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.AllMarkterResponseDto;
import com.hrth.ustock.dto.stock.SkrrrCalculatorRequestDto;
import com.hrth.ustock.dto.stock.SkrrrCalculatorResponseDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
@Tag(name = "Stocks", description = "종목 관련 API")
public class StockController {
    private final StockService stockService;

    @GetMapping
    @Operation(summary = "종목 순위 리스트", description = "시가총액, 거래량, 등락율 순 종목 리스트 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = StockResponseDto.class))
            )
    )
    public ResponseEntity<?> stockList(@RequestParam String order) {
        List<StockResponseDto> stockMap = stockService.getStockList(order);

        return ResponseEntity.ok(stockMap);
    }

    @GetMapping("/market")
    @Operation(summary = "코스피, 코스닥 지수", description = "현재 코스피, 코스닥 지수 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AllMarkterResponseDto.class)
            )
    )
    public ResponseEntity<?> marketInformation() {
        AllMarkterResponseDto marketInfo = stockService.getMarketInfo();

        return ResponseEntity.ok(marketInfo);
    }

    @GetMapping("/search")
    @Operation(summary = "종목 검색", description = "종목 검색결과 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = StockResponseDto.class))
            )
    )
    public ResponseEntity<?> searchStock(@RequestParam String query) {
        List<StockResponseDto> stockList = stockService.searchStock(query);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/{code}")
    @Operation(summary = "종목 조회", description = "종목코드 기반 특정 종목 조회결과 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StockResponseDto.class)
            )
    )
    public ResponseEntity<?> findStockByCode(@PathVariable String code) {
        StockResponseDto stockResponseDto = stockService.getStockInfo(code);

        return ResponseEntity.ok(stockResponseDto);
    }

    @GetMapping("/{code}/chart")
    @Operation(summary = "차트 조회", description = "특정 종목 차트결과 반환(일, 주, 월봉 확인가능)")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ChartResponseDto.class))
            )
    )
    public ResponseEntity<?> getStockChart(@PathVariable String code, @RequestParam int period) {
        List<ChartResponseDto> list = stockService.getStockChartAndNews(code, period);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}/skrrr")
    @Operation(summary = "스껄계산기", description = "특정 종목의 특정 날짜 현재가 기준 계산 결과 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SkrrrCalculatorResponseDto.class)
            )
    )
    public ResponseEntity<?> skrrrCalculator(@PathVariable String code, @ModelAttribute SkrrrCalculatorRequestDto requestDto) {
        SkrrrCalculatorResponseDto skrrrCalculatorResponseDto = stockService.calculateSkrrr(code, requestDto);

        return ResponseEntity.ok(skrrrCalculatorResponseDto);
    }
}