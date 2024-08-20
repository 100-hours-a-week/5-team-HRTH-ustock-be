package com.hrth.ustock.controller;

import com.hrth.ustock.dto.stock.MarketResponseDto;
import com.hrth.ustock.dto.stock.StockDTO;
import com.hrth.ustock.dto.stock.StockListDTO;
import com.hrth.ustock.dto.stock.StockResponseDTO;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {
    private final StockService stockService;

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

        StockResponseDTO stockResponseDTO = stockService.getStockInfo(code);
        return ResponseEntity.ok(stockResponseDTO);
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
