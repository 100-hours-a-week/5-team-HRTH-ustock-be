package com.hrth.ustock.controller;

import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.StockDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {

    private final StockService stockService;

    private static final String DATE_PATTERN = "^[0-9]{4}/[0-9]{2}/[0-9]{2}$";
    private static final Pattern pattern = Pattern.compile(DATE_PATTERN);

    // 6. 종목 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchStock(@RequestParam String query) {

        if(query.length() > 10) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<StockDto> stockList = stockService.findByStockName(query);
            return ResponseEntity.ok(stockList);
        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 14. 주식 상세정보 조회
    @GetMapping("/{code}")
    public ResponseEntity<?> findStockByCode(@PathVariable String code) {

        if( code.length() != 6) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StockResponseDto stockResponseDTO = stockService.getStockInfo(code);
            return ResponseEntity.ok(stockResponseDTO);
        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ChartNotFoundException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 15. 종목 차트 조회
    @GetMapping("/{code}/chart")
    public ResponseEntity<?> getStockChart(
            @PathVariable String code, @RequestParam int period, @RequestParam String start, @RequestParam String end) {

        if (!pattern.matcher(start).matches() || !pattern.matcher(end).matches() ||
                period < 1 || period > 4 || code.length()!=6) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<ChartResponseDto> chartListResponseDTO = stockService.getStockChartAndNews(code, period, start, end);
            return ResponseEntity.ok(chartListResponseDTO);
        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
