package com.hrth.ustock.service;

import com.hrth.ustock.dto.chart.ChartDto;
import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.StockDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.entity.portfolio.Chart;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.NewsRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.util.DateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
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
    private final NewsRepository newsRepository;
    private final DateConverter dateConverter;


    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final ZonedDateTime MARKET_OPEN = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.now(KST_ZONE), LocalTime.of(9, 0)),
            KST_ZONE
    );
    private static final ZonedDateTime MARKET_CLOSE = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.now(KST_ZONE), LocalTime.of(15, 30)),
            KST_ZONE
    );

    private static final int TEMP_CURRENT = 100000;
    private static final double TEMP_CHANGE_RATE = 22.0;


    // 6. 종목 검색
    @Transactional
    public List<StockDto> findByStockName(String str) {

        List<Stock> list = stockRepository.findAllByNameContaining(str);

        // TODO: 현재가, 등락율 추가
        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }

        List<StockDto> stockDtoList = new ArrayList<>();
        for (Stock stock : list) {
            StockDto stockDTO = stock.toDto();
            stockDTO.setPrice(getCurrentPrice(stock.getCode()));
            stockDTO.setChangeRate(getChangeRate(stock.getCode()));
            stockDtoList.add(stockDTO);
        }

        return stockDtoList;
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public StockResponseDto getStockInfo(String code) {

        Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
        Chart chart = chartRepository.findByStockCodeOrderByIdDesc(code).orElseThrow(ChartNotFoundException::new);

        // 정규시장: 09:00~15:30, 이 시간에는 현재가로 등락/등락율 계산, 외에는 차트 데이터로 등락/등락율 계산
        // 1차 구현: 전일 종가 = 현재가
        // TODO: 현재가 develop
        ZonedDateTime now = ZonedDateTime.now(KST_ZONE);
        int current = getCurrentPrice(stock.getCode());
        int increase;
        if (now.isAfter(MARKET_OPEN) && now.isBefore(MARKET_CLOSE)) {
            increase = current - chart.getOpen();
        } else {
            increase = current - chart.getClose();
        }

        return StockResponseDto.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .price(current)
                .change(increase)
                .changeRate(getChangeRate(stock.getCode()))
                .build();
    }

    // 15. 종목 차트 조회
    public List<ChartResponseDto> getStockChartAndNews(String code, int period, String start, String end) {
        // 1: 일봉, 2: 주봉, 3: 월봉, 4: 연봉
        return switch (period) {
            case 1 -> getChartByRangeList(code, dateConverter.getDailyRanges(start, end));
            case 2 -> getChartByRangeList(code, dateConverter.getWeeklyRanges(start, end));
            case 3 -> getChartByRangeList(code, dateConverter.getMonthlyRanges(start, end));
            case 4 -> getChartByRangeList(code, dateConverter.getYearlyRanges(start, end));
            default -> null;
        };
    }

    // 현재가 조회
    private int getCurrentPrice(String code) {
        // TODO: 현재가 api로 갱신
        return TEMP_CURRENT;
    }

    // 등락율 조회
    private double getChangeRate(String code) {
        return TEMP_CHANGE_RATE;
    }

    // chart, news 범위 조회
    private List<ChartResponseDto> getChartByRangeList(String code, List<Pair<String, String>> dateList) {
        List<ChartResponseDto> chartListResponse = new ArrayList<>();
        for (Pair<String, String> data : dateList) {
            ChartResponseDto chartResponseDTO = new ChartResponseDto();
            chartResponseDTO.setCandle(ChartDto.builder().build());
            chartResponseDTO.getCandle().setHigh(0);
            chartResponseDTO.getCandle().setLow(1000000000);

            chartRepository.findAllByStockCodeAndDateBetween(code, data.getFirst(), data.getSecond()).forEach(chart -> {
                chartResponseDTO.setTime(data.getFirst());
                if (chart.getDate().equals(data.getFirst())) {
                    chartResponseDTO.getCandle().setOpen(chart.getOpen());
                }
                if (chart.getDate().equals(data.getSecond())) {
                    chartResponseDTO.getCandle().setClose(chart.getClose());
                }
                if (chart.getHigh() > chartResponseDTO.getCandle().getHigh()) {
                    chartResponseDTO.getCandle().setHigh(chart.getHigh());
                }
                if (chart.getLow() < chartResponseDTO.getCandle().getLow()) {
                    chartResponseDTO.getCandle().setLow(chart.getLow());
                }
            });
            newsRepository.findAllByStockCodeAndDateBetween(code, data.getFirst(), data.getSecond()).forEach(news -> {
                chartResponseDTO.getNews().add(news.toEmbedDto());
            });
            chartListResponse.add(chartResponseDTO);
        }
        return chartListResponse;
    }
}
