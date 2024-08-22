package com.hrth.ustock.service;

import com.hrth.ustock.dto.chart.ChartDto;
import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.MarketResponseDto;
import com.hrth.ustock.dto.stock.StockDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.NewsRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.util.DateConverter;
import com.hrth.ustock.util.KisApiAuthManager;
import com.hrth.ustock.util.RedisTTLCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private final RedisTTLCalculator redisTTLCalculator;
    private final RedisTemplate<String, String> redisTemplate;

    private final RestClient restClient;
    private final KisApiAuthManager authManager;

    // 6. 종목 검색
    @Transactional
    public List<StockDto> findByStockName(String str) {

        List<Stock> list = stockRepository.findAllByNameContaining(str);

        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }

        List<StockDto> stockDtoList = new ArrayList<>();
        for (Stock stock : list) {
            String currentSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "current");
            String rateSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "changeRate");

            if (currentSaved == null || rateSaved == null) {
                cacheCurrentChangeChangeRate(stock.getCode());
                currentSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "current");
                rateSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "changeRate");
            }

            StockDto stockDTO = stock.toDto();
            stockDTO.setPrice(Integer.parseInt(currentSaved));
            stockDTO.setChangeRate(Double.parseDouble(rateSaved));
            stockDtoList.add(stockDTO);
        }

        return stockDtoList;
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public StockResponseDto getStockInfo(String code) {
        Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);

        String currentSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "current");
        String changeSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "change");
        String rateSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "changeRate");

        if (currentSaved == null || changeSaved == null || rateSaved == null) {
            cacheCurrentChangeChangeRate(stock.getCode());
            currentSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "current");
            changeSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "change");
            rateSaved = (String) redisTemplate.opsForHash().get(stock.getCode(), "changeRate");
        }

        return StockResponseDto.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .logo(stock.getLogo())
                .price(Integer.parseInt(currentSaved))
                .change(Integer.parseInt(changeSaved))
                .changeRate(Double.parseDouble(rateSaved))
                .build();
    }

    // 15. 종목 차트 조회
    public List<ChartResponseDto> getStockChartAndNews(String code, int period) {
        // 1: 일봉, 2: 주봉, 3: 월봉
        return switch (period) {
            case 1 -> getChartByRangeList(code, dateConverter.getDailyRanges());
            case 2 -> getChartByRangeList(code, dateConverter.getWeeklyRanges());
            case 3 -> getChartByRangeList(code, dateConverter.getMonthlyRanges());
            default -> null;
        };
    }

    // chart, news 범위 조회
    private List<ChartResponseDto> getChartByRangeList(String code, List<Pair<String, String>> dateList) {
        List<ChartResponseDto> chartListResponse = new ArrayList<>();

        for (Pair<String, String> data : dateList) {
            ChartResponseDto chartResponseDTO = new ChartResponseDto();
            chartResponseDTO.setCandle(ChartDto.builder().build());
            chartResponseDTO.setNews(new ArrayList<>());
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

    // 현재가, 전일대비, 전일 대비 부호, 전일 대비율 조회
    public void cacheCurrentChangeChangeRate(String code) {
        Map<String, String> response = requestPrice(code);

        long second = redisTTLCalculator.calculateTTLForMidnightKST();

        redisTemplate.opsForHash().put(code, "current", response.get(STOCK_CURRENT_PRICE));
        redisTemplate.opsForHash().put(code, "change", response.get(CHANGE_FROM_PREVIOUS_STOCK));
        redisTemplate.opsForHash().put(code, "changeRate", response.get(CHANGE_RATE_FROM_PREVIOUS_STOCK));
        redisTemplate.expire(code, second, TimeUnit.SECONDS);
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

    private Map<String, String> requestPrice(String code) {
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + code;

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/inquire-price" + queryParams)
                .headers(setRequestHeaders("FHKST01010100"))
                .retrieve()
                .body(Map.class);

        return (Map<String, String>) response.get("output");
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
