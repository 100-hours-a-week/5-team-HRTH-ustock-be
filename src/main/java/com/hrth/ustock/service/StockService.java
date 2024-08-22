package com.hrth.ustock.service;

import com.hrth.ustock.dto.chart.ChartDto;
import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.MarketResponseDto;
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
import com.hrth.ustock.util.KisApiAuthManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.hrth.ustock.service.StockServiceConst.*;

@Service
@RequiredArgsConstructor
public class StockService {
    public static final int TOP_RANK_RANGE = 5;

    private final StockRepository stockRepository;
    private final ChartRepository chartRepository;
    private final NewsRepository newsRepository;
    private final DateConverter dateConverter;


    private final RestClient restClient;
    private final KisApiAuthManager authManager;

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
    public List<StockDto> findByStockName(String name) {

        List<Stock> list = stockRepository.findByNameStartingWith(name);
        list.addAll(stockRepository.findByNameContainingButNotStartingWith(name));

        // TODO: 현재가, 등락율 추가
        if (list.isEmpty()) {
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
        Chart chart = chartRepository.findByStockCode(code).orElseThrow(ChartNotFoundException::new);

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
        // TODO: 현재가 api 로 갱신
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
                chartResponseDTO.setDate(data.getFirst());
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

    public Map<String, Object> getMarketInfo() {
        Map<String, String> kospi = requestMarketInfo(KOSPI_CODE);
        Map<String, String> kosdaq = requestMarketInfo(KOSDAQ_CODE);

        Map<String, Object> marketInfo = new HashMap<>();
        marketInfo.put("kospi", makeMarketDto(kospi));
        marketInfo.put("kosdaq", makeMarketDto(kosdaq));

        return marketInfo;
    }

    private Map<String, String> requestMarketInfo(String marketCode) {
        String queryParams = "?fid_cond_mrkt_div_code=U"
                + "&fid_input_iscd=" + marketCode;

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/inquire-index-price" + queryParams)
                .headers(setRequestHeaders("FHPUP02100000"))
                .retrieve()
                .body(Map.class);

        return (Map<String, String>) response.get("output");
    }

    private MarketResponseDto makeMarketDto(Map<String, String> responseMap) {
        double price = Double.parseDouble(responseMap.get(MARKET_CURRENT_PRICE));
        double change = Double.parseDouble(responseMap.get(CHANGE_FROM_PREVIOUS_MARKET));
        double changeRate = Double.parseDouble(responseMap.get(CHANGE_RATE_FROM_PREVIOUS_MARKET));

        return MarketResponseDto.builder()
                .price(price)
                .change(change)
                .changeRate(changeRate)
                .build();
    }

    public Map<String, List<StockResponseDto>> getStockList(String order) {
        List<Map<String, String>> responseList = switch (order) {
            case "top", "trade" -> requestOrderByTrade();
            case "capital" -> requestOrderByCapital();
            case "change" -> requestOrderByChange();
            default -> throw new IllegalArgumentException();
        };

        List<StockResponseDto> stockList = new ArrayList<>();

        int range = "top".equals(order) ? TOP_RANK_RANGE : responseList.size();
        for (int i = 0; i < range; i++) {
            stockList.add(makeStockResponseDto(responseList.get(i)));
        }

        Map<String, List<StockResponseDto>> stockMap = new HashMap<>();
        stockMap.put("stock", stockList);

        return stockMap;
    }

    private List<Map<String, String>> requestOrderByTrade() {
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20171" +
                "&fid_div_cls_code=1" +
                "&fid_input_iscd=0000" +
                "&fid_trgt_cls_code=0" +
                "&fid_blng_cls_code=0" +
                "&fid_trgt_cls_code=111111111" +
                "&fid_trgt_exls_cls_code=1111111111" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_input_date_1=";

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/volume-rank" + queryParams)
                .headers(setRequestHeaders("FHPST01710000"))
                .retrieve()
                .body(Map.class);

        return (List<Map<String, String>>) response.get("output");
    }

    private List<Map<String, String>> requestOrderByCapital() {
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20174" +
                "&fid_div_cls_code=1" +
                "&fid_input_iscd=0000" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=";

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/ranking/market-cap" + queryParams)
                .headers(setRequestHeaders("FHPST01740000"))
                .retrieve()
                .body(Map.class);

        return (List<Map<String, String>>) response.get("output");
    }

    private List<Map<String, String>> requestOrderByChange() {
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20170" +
                "&fid_input_iscd=0000" +
                "&fid_rank_sort_cls_code=0" +
                "&fid_input_cnt_1=0" +
                "&fid_prc_cls_code=1" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_div_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_rsfl_rate1=" +
                "&fid_rsfl_rate2=" +
                "&fid_vol_cnt=";

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/ranking/fluctuation" + queryParams)
                .headers(setRequestHeaders("FHPST01700000"))
                .retrieve()
                .body(Map.class);

        return (List<Map<String, String>>) response.get("output");
    }

    private StockResponseDto makeStockResponseDto(Map<String, String> responseMap) {
        Stock stock = stockRepository.findByCode(responseMap.get(STOCK_CODE)).orElseThrow(StockNotFoundException::new);

        return StockResponseDto.builder()
                .name(responseMap.get(STOCK_NAME))
                .code(responseMap.get(STOCK_CODE))
                .logo(stock.getLogo())
                .price(Integer.parseInt(responseMap.get(STOCK_CURRENT_PRICE)))
                .change(Integer.parseInt(responseMap.get(CHANGE_FROM_PREVIOUS_STOCK)))
                .changeRate(Double.parseDouble(responseMap.get(CHANGE_RATE_FROM_PREVIOUS_STOCK)))
                .build();
    }

    private Consumer<HttpHeaders> setRequestHeaders(String trId) {
        return httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setBearerAuth(authManager.generateToken());
            httpHeaders.set("appkey", authManager.getAppKey());
            httpHeaders.set("appsecret", authManager.getAppSecret());
            httpHeaders.set("tr_id", trId);
            httpHeaders.set("custtype", "P");
        };
    }
}
