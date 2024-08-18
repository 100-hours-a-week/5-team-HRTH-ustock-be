package com.hrth.ustock.controller;

import com.hrth.ustock.dto.stock.StockDTO;
import com.hrth.ustock.dto.stock.StockListDTO;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stocks")
public class StockController {
    private final StockService stockService;

    // 5. 종목 5개 차트 조회
    // 구현전
    // return: name, code, price(현재가), changeRate(등락율)
    // redis에 매일 카테고리별로 상위 종목 5개 코드를 리스트로 저장? - 데이터 갱신할때 같이 받아오면 되지않을까

    // 6. 종목 검색
    // TODO: price, changeRate 반영
    // return: stock: [{name, code, price, changeRate}, ...]
    @GetMapping("/search")
    public ResponseEntity<StockListDTO> searchStock(@RequestParam String query) {
        try {
            List<StockDTO> stock = stockService.findByStockName(query);
            StockListDTO stockList = StockListDTO.builder()
                    .stocks(stock)
                    .build();
            return ResponseEntity.ok(stockList);
        } catch (StockNotFoundException e) {
            // stock not found
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // argument not match
            return ResponseEntity.badRequest().build();
        }
    }

    // 14. 주식 상세정보 조회
    @GetMapping("/{code}")
    public ResponseEntity<?> findStockByCode(@PathVariable String code) {
        return stockService.getStockInfo(code);
    }
}
