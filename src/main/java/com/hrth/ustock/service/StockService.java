package com.hrth.ustock.service;

import com.hrth.ustock.dto.stock.StockDTO;
import com.hrth.ustock.dto.stock.StockListDTO;
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

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final ChartRepository chartRepository;

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final ZonedDateTime MARKET_OPEN = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.now(KST_ZONE), LocalTime.of(9, 0)),
            KST_ZONE
    );
    private static final ZonedDateTime MARKET_CLOSE = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.now(KST_ZONE), LocalTime.of(15, 30)),
            KST_ZONE
    );

    // 6. 종목 검색
    public StockListDTO findByStockName(String str) {

        List<Stock> list = stockRepository.findAllByNameContaining(str);

        // TODO: 현재가, 등락율 추가
        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }
        List<StockDTO> StockDTOList = new ArrayList<>();
        for (Stock stock : list) {
            StockDTOList.add(stock.toDTO());
        }

        return StockListDTO.builder()
                .stocks(StockDTOList)
                .build();
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public StockResponseDTO getStockInfo(String code) {

        Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
        List<Chart> charts = chartRepository.findTop2ByStockCodeOrderByIdDesc(code);

        if (charts == null || charts.isEmpty())
            throw new ChartNotFoundException();

        // 정규시장: 09:00~15:30, 이 시간에는 현재가로 등락/등락율 계산, 외에는 차트 데이터로 등락/등락율 계산
        // 1차 구현: 전일 종가 = 현재가
        // TODO: 현재가 develop
        ZonedDateTime now = ZonedDateTime.now(KST_ZONE);
        int current = getCurrentPrice(charts.get(0));
        int increase;
        double rate;
        if (now.isAfter(MARKET_OPEN) && now.isBefore(MARKET_CLOSE)) {
            int yesterday = charts.get(0).getClose();
            increase = current - yesterday;
            rate = (yesterday==0) ? 0.0 : (double) increase / yesterday * 100;
        } else {
            increase = charts.get(0).getClose() - charts.get(1).getClose();
            rate = (charts.get(1).getClose()==0) ? 0.0 : (double) increase / charts.get(1).getClose() * 100;
        }

        return StockResponseDTO.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .price(current)
                .change(increase)
                .changeRate(rate)
                .build();
    }

    // 현재가 조회
    public int getCurrentPrice(Chart chart) {
        // TODO: 현재가 api로 갱신
        return chart.getClose();
    }
}
