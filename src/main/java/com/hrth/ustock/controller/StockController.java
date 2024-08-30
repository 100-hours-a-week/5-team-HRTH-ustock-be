package com.hrth.ustock.controller;

import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.MarketResponseDto;
import com.hrth.ustock.dto.stock.SkrrrCalculatorRequestDto;
import com.hrth.ustock.dto.stock.SkrrrCalculatorResponseDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.exception.*;
import com.hrth.ustock.service.StockService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<?> stockList(@RequestParam String order) {
        Map<String, List<StockResponseDto>> stockMap = stockService.getStockList(order);

        return ResponseEntity.ok(stockMap);
    }

    @GetMapping("/market")
    public ResponseEntity<?> marketInformation() {
        Map<String, MarketResponseDto> marketInfo = stockService.getMarketInfo();

        return ResponseEntity.ok(marketInfo);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchStock(@RequestParam String query) {
        List<StockResponseDto> stockList = stockService.searchStock(query);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> findStockByCode(@PathVariable String code) {
        StockResponseDto stockResponseDto = stockService.getStockInfo(code);

        return ResponseEntity.ok(stockResponseDto);
    }

    @GetMapping("/{code}/chart")
    public ResponseEntity<?> getStockChart(@PathVariable String code, @RequestParam int period) {
        List<ChartResponseDto> list = stockService.getStockChartAndNews(code, period);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}/skrrr")
    public ResponseEntity<?> skrrrCalculator(@PathVariable String code, @ModelAttribute SkrrrCalculatorRequestDto requestDto) {
        SkrrrCalculatorResponseDto skrrrCalculatorResponseDto = stockService.calculateSkrrr(code, requestDto);

        return ResponseEntity.ok(skrrrCalculatorResponseDto);
    }
}