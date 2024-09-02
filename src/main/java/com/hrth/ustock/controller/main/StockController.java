package com.hrth.ustock.controller.main;

import com.hrth.ustock.dto.main.chart.ChartResponseDto;
import com.hrth.ustock.dto.main.stock.AllMarkterResponseDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorRequestDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorResponseDto;
import com.hrth.ustock.dto.main.stock.StockResponseDto;
import com.hrth.ustock.service.main.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
@Tag(name = "Stock", description = "종목 관련 API")
public class StockController {
    private final StockService stockService;

    @GetMapping
    @Operation(summary = "종목 순위 리스트", description = "시가총액, 거래량, 등락율 순 종목 리스트 반환")
    public ResponseEntity<List<StockResponseDto>> stockList(@RequestParam String order) {
        List<StockResponseDto> stockList = stockService.getStockList(order);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/market")
    @Operation(summary = "코스피, 코스닥 지수", description = "현재 코스피, 코스닥 지수 반환")
    public ResponseEntity<AllMarkterResponseDto> marketInformation() {
        AllMarkterResponseDto marketInfo = stockService.getMarketInfo();

        return ResponseEntity.ok(marketInfo);
    }

    @GetMapping("/search")
    @Operation(summary = "종목 검색", description = "종목 검색결과 반환")
    public ResponseEntity<List<StockResponseDto>> searchStock(@RequestParam String query) {
        List<StockResponseDto> stockList = stockService.searchStock(query);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/{code}")
    @Operation(summary = "종목 조회", description = "종목코드 기반 특정 종목 조회결과 반환")
    public ResponseEntity<StockResponseDto> findStockByCode(@PathVariable String code) {
        StockResponseDto stockResponseDto = stockService.getStockInfo(code);

        return ResponseEntity.ok(stockResponseDto);
    }

    @GetMapping("/{code}/chart")
    @Operation(summary = "차트 조회", description = "특정 종목 차트결과 반환(일, 주, 월봉 확인가능)")
    public ResponseEntity<List<ChartResponseDto>> getStockChart(@PathVariable String code, @RequestParam int period) {
        List<ChartResponseDto> list = stockService.getStockChartAndNews(code, period);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}/skrrr")
    @Operation(summary = "스껄계산기", description = "특정 종목의 특정 날짜 현재가 기준 계산 결과 반환")
    public ResponseEntity<SkrrrCalculatorResponseDto> skrrrCalculator(@PathVariable String code, @ModelAttribute SkrrrCalculatorRequestDto requestDto) {
        SkrrrCalculatorResponseDto skrrrCalculatorResponseDto = stockService.calculateSkrrr(code, requestDto);

        return ResponseEntity.ok(skrrrCalculatorResponseDto);
    }
}