package com.hrth.ustock.controller.main;

import com.hrth.ustock.controller.api.StockApi;
import com.hrth.ustock.dto.main.chart.ChartResponseDto;
import com.hrth.ustock.dto.main.stock.AllMarketResponseDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorRequestDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorResponseDto;
import com.hrth.ustock.dto.main.stock.StockResponseDto;
import com.hrth.ustock.service.main.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController implements StockApi {
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockResponseDto>> stockList(@RequestParam String order) {
        List<StockResponseDto> stockList = stockService.getStockList(order);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/market")
    public ResponseEntity<AllMarketResponseDto> marketInformation() {
        AllMarketResponseDto marketInfo = stockService.getMarketInfo();

        return ResponseEntity.ok(marketInfo);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockResponseDto>> searchStock(@RequestParam String query) {
        List<StockResponseDto> stockList = stockService.searchStock(query);

        return ResponseEntity.ok(stockList);
    }

    @GetMapping("/{code}")
    public ResponseEntity<StockResponseDto> findStockByCode(@PathVariable String code) {
        StockResponseDto stockResponseDto = stockService.getStockInfo(code);

        return ResponseEntity.ok(stockResponseDto);
    }

    @GetMapping("/{code}/chart")
    public ResponseEntity<List<ChartResponseDto>> getStockChart(@PathVariable String code, @RequestParam int period) {
        List<ChartResponseDto> list = stockService.getStockChartAndNews(code, period);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}/skrrr")
    public ResponseEntity<SkrrrCalculatorResponseDto> skrrrCalculator(@PathVariable String code, @ModelAttribute SkrrrCalculatorRequestDto requestDto) {
        SkrrrCalculatorResponseDto skrrrCalculatorResponseDto = stockService.calculateSkrrr(code, requestDto);

        return ResponseEntity.ok(skrrrCalculatorResponseDto);
    }
}