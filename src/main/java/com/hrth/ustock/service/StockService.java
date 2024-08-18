package com.hrth.ustock.service;

import com.hrth.ustock.dto.stock.StockDTO;
import com.hrth.ustock.dto.stock.StockResponseDTO;
import com.hrth.ustock.entity.portfolio.Chart;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final ChartRepository chartRepository;
//    private final RedisTemplate<String, Integer> redisCurrentTemplate;

    // 한국 정규시장 개장/마감
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 0);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    // 6. 종목 검색
    public List<StockDTO> findByStockName(String str) {
        List<Stock> list = stockRepository.findAllByNameContaining(str);
        // TODO: 현재가, 등락율 추가
        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }
        List<StockDTO> stockList = new ArrayList<>();
        for (Stock stock : list) {
            stockList.add(stock.toDTO());
        }
        return stockList;
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public ResponseEntity<?> getStockInfo(String code) {
        try {
            Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);

            // 필요한 정보: 종목코드, 종목명, 현재가, 전일대비 등락, 전일대비 등락율
            List<Chart> charts = chartRepository.findTop2ByStockCodeOrderByIdDesc(code);
            if (charts == null || charts.isEmpty()) {
                throw new ChartNotFoundException();
            } else {
                // 정규시장: 09:00~15:30, 이 시간에는 현재가로 등락/등락율 계산, 외에는 차트 데이터로 등락/등락율 계산
                // 공식: 증감 = (현재가 or 종가) - 어제 종가, 증감률(Double) = 증감 / 어제 종가*100
                // 만약 장마감 전이라면 | 장마감 후라면
                // charts.get(0) -> 전일 | charts.get(0) -> 금일
                // charts.get(1) -> 사용하지 않음 | charts.get(1) -> 전일

                // 1차 구현: 전일 종가 = 현재가
                // TODO: 현재가 develop
                LocalTime now = LocalTime.now();
                int current = getCurrentPrice(charts.get(0));
                int increase = 0;
                Double rate = 0.0;
                if (now.isAfter(MARKET_OPEN) && now.isBefore(MARKET_CLOSE)) {
                    // 장마감 전
                    int yesterday = charts.get(0).getClose();
                    increase = current - yesterday;
                    rate = (double) increase / yesterday * 100;
                } else {
                    // 장마감 후
                    increase = charts.get(0).getClose() - charts.get(1).getClose();
                    rate = (double) increase / charts.get(1).getClose() * 100;
                }
                if (rate.isNaN() || rate.isInfinite()) {
                    return ResponseEntity.internalServerError().build();
                }

                StockResponseDTO stockResponseDTO = StockResponseDTO.builder()
                        .code(stock.getCode())
                        .name(stock.getName())
                        .price(current)
                        .change(increase)
                        .changeRate(rate)
                        .build();
                return ResponseEntity.ok(stockResponseDTO);
            }

        } catch (StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ChartNotFoundException e) {
            // chart data는 미리 넣어둘거라 조회안되면 서버 내부 오류
            return ResponseEntity.internalServerError().build();
        }
    }

    // 현재가 조회
    public int getCurrentPrice(Chart chart) {
        // TODO: 현재가 api로 갱신
        return chart.getClose();
        // 추후 develop 시에 사용할 코드 (redis에 현재가 갱신)
//        Integer current = redisCurrentTemplate.opsForValue().get(code);
//        if(current == null) {
//            // 한국투자증권 api 연결, 현재가 가져오기
//            int price = 100000;
//            redisCurrentTemplate.opsForValue().set(code, price, 5, TimeUnit.MINUTES);
//            return price;
//        } else {
//            return current;
//        }
    }
}
