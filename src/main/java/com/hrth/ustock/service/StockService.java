package com.hrth.ustock.service;

import com.hrth.ustock.dto.stock.MarketResponseDto;
import com.hrth.ustock.dto.stock.StockDto;
import com.hrth.ustock.dto.stock.StockListDTO;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.entity.portfolio.Chart;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.util.KisApiAuthManager;
import lombok.RequiredArgsConstructor;
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

    // 6. 종목 검색
    public StockListDTO findByStockName(String str) {

        List<Stock> list = stockRepository.findAllByNameContaining(str);

        // TODO: 현재가, 등락율 추가
        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }
        List<StockDto> stockDtoList = new ArrayList<>();
        for (Stock stock : list) {
            stockDtoList.add(stock.toDTO());
        }

        return StockListDTO.builder()
                .stocks(stockDtoList)
                .build();
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public StockResponseDto getStockInfo(String code) {

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
            rate = (yesterday == 0) ? 0.0 : (double) increase / yesterday * 100;
        } else {
            increase = charts.get(0).getClose() - charts.get(1).getClose();
            rate = (charts.get(1).getClose() == 0) ? 0.0 : (double) increase / charts.get(1).getClose() * 100;
        }

        return StockResponseDto.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .price(current)
                .change(increase)
                .changeRate(rate)
                .build();
    }

    // 현재가 조회
    private int getCurrentPrice(Chart chart) {
        // TODO: 현재가 api로 갱신
        return chart.getClose();
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
