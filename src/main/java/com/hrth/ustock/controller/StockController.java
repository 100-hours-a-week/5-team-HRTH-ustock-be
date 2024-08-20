package com.hrth.ustock.controller;

import com.hrth.ustock.dto.stock.StockListDTO;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        List<StockResponseDto> stockList;
        try {
            stockList = stockService.getStockList(order);
        } catch (StockNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주식 목록을 찾을 수 없습니다.");
        }

        if (stockList == null)
            return ResponseEntity.badRequest().body("잘못된 정렬 기준입니다.");

        return ResponseEntity.ok(stockList);
    }

    // 6. 종목 검색
    @GetMapping("/search")
    public ResponseEntity<StockListDTO> searchStock(@RequestParam String query) {

        try {
            StockListDTO stockList = stockService.findByStockName(query);
            return ResponseEntity.ok(stockList);
        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 14. 주식 상세정보 조회
    @GetMapping("/{code}")
    public ResponseEntity<?> findStockByCode(@PathVariable String code) {
        StockResponseDto stockResponseDto;
        try {
            stockResponseDto = stockService.getStockInfo(code);
        } catch (ChartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("차트 정보를 조회할 수 없습니다.");
        }

        return ResponseEntity.ok(stockResponseDto);
    }

    @GetMapping("/market")
    public ResponseEntity<?> marketInformation() {
        Map<String, Object> marketInfo;
        try {
            marketInfo = stockService.getMarketInfo();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("한국투자 API 조회 실패");
        }

        return ResponseEntity.ok(marketInfo);
    }
}
