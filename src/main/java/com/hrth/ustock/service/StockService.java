package com.hrth.ustock.service;

import com.hrth.ustock.dto.chart.ChartDto;
import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.*;
import com.hrth.ustock.entity.portfolio.Chart;
import com.hrth.ustock.entity.portfolio.News;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.ChartNotFoundException;
import com.hrth.ustock.exception.CurrentNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.exception.StockNotPublicException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.NewsRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.util.DateConverter;
import com.hrth.ustock.util.KisApiAuthManager;
import com.hrth.ustock.util.RedisTTLCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.hrth.ustock.service.StockServiceConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private static final int TOP_RANK_RANGE = 5;

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
    public List<StockDto> findByStockName(String name) {

        List<Stock> list = stockRepository.findByNameStartingWith(name);
        list.addAll(stockRepository.findByNameContainingButNotStartingWith(name));

        if (list.isEmpty()) {
            throw new StockNotFoundException();
        }

        List<StockDto> stockDtoList = new ArrayList<>();
        for (Stock stock : list) {
            Map<String, String> redisMap = getCurrentChangeChangeRate(stock.getCode());

            if(redisMap == null) {
                log.info("no current for stock: {}", stock.getCode());
                continue;
            }

            String currentSaved = redisMap.get(REDIS_CURRENT_KEY);
            String rateSaved = redisMap.get(REDIS_CHANGE_RATE_KEY);

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

        Map<String, String> redisMap = getCurrentChangeChangeRate(stock.getCode());
        if(redisMap == null) {
            log.info("no current for stock: {}", stock.getCode());
            throw new CurrentNotFoundException();
        }

        String currentSaved = redisMap.get(REDIS_CURRENT_KEY);
        String changeSaved = redisMap.get(REDIS_CHANGE_KEY);
        String rateSaved = redisMap.get(REDIS_CHANGE_RATE_KEY);

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
        String start = dateConverter.getStartDateOneYearAgo();
        String end = dateConverter.getCurrentDate();
        return switch (period) {
            case 1 -> getChartByRangeList(code, dateConverter.getDailyRanges(start, end), start, end);
            case 2 -> getChartByRangeList(code, dateConverter.getWeeklyRanges(start, end), start, end);
            case 3 -> getChartByRangeList(code, dateConverter.getMonthlyRanges(start, end), start, end);
            default -> null;
        };
    }

    // chart, news 범위 조회
    private List<ChartResponseDto> getChartByRangeList(String code, List<Pair<String, String>> dateList, String start, String end) {
        List<ChartResponseDto> chartListResponse = new ArrayList<>();
        List<Chart> chartList = chartRepository.findAllByStockCodeAndDateBetween(code, start, end);
        List<News> newsList = newsRepository.findAllByStockCodeAndDateBetween(code, start, end);

        if (chartList == null || chartList.isEmpty()) {
            throw new ChartNotFoundException();
        }

        for (Pair<String, String> data : dateList) {
            String startDate = data.getFirst();
            String endDate = data.getSecond();

            ChartResponseDto chartResponseDto = new ChartResponseDto();
            chartResponseDto.setCandle(ChartDto.builder().build());
            chartResponseDto.setNews(new ArrayList<>());
            chartResponseDto.getCandle().setHigh(0);
            chartResponseDto.getCandle().setLow(10000000);
            chartResponseDto.setDate(startDate);

            chartList.stream()
                    .filter(chart ->
                            chart.getDate().compareTo(startDate) >= 0 && chart.getDate().compareTo(endDate) <= 0)
                    .forEach(chart -> {
                        if (chart.getDate().equals(startDate)) {
                            chartResponseDto.getCandle().setOpen(chart.getOpen());
                        }
                        // 종가 0으로 반환하지 않도록 수정
                        chartResponseDto.getCandle().setClose(chart.getClose());
                        if (chart.getHigh() > chartResponseDto.getCandle().getHigh()) {
                            chartResponseDto.getCandle().setHigh(chart.getHigh());
                        }
                        if (chart.getLow() < chartResponseDto.getCandle().getLow()) {
                            chartResponseDto.getCandle().setLow(chart.getLow());
                        }
                    });

            if (chartResponseDto.getCandle().getHigh() == 0) {
                continue;
            } else if (chartResponseDto.getCandle().getOpen() == 0) {
                // 시가 0으로 반환하지 않도록 수정
                while (chartResponseDto.getCandle().getOpen() == 0) {
                    chartList.forEach(chart -> {
                        chartResponseDto.getCandle().setHigh(chart.getOpen());
                    });
                }
            }

            newsList.stream()
                    .filter(news ->
                            news.getDate().compareTo(startDate) >= 0 && news.getDate().compareTo(endDate) <= 0)
                    .forEach(news -> chartResponseDto.getNews().add(news.toEmbedDto()));
            chartListResponse.add(chartResponseDto);
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
            stockList.add(makeStockResponseDto(responseList.get(i), order));
        }

        Map<String, List<StockResponseDto>> stockMap = new HashMap<>();
        stockMap.put("stock", stockList);

        return stockMap;
    }

    // 현재가, 전일대비, 전일 대비 부호, 전일 대비율 조회
    protected Map<String, String> getCurrentChangeChangeRate(String code) {
        String current = (String) redisTemplate.opsForHash().get(code, REDIS_CURRENT_KEY);
        String change = (String) redisTemplate.opsForHash().get(code, REDIS_CHANGE_KEY);
        String changeRate = (String) redisTemplate.opsForHash().get(code, REDIS_CHANGE_RATE_KEY);

        if (current != null && change != null && changeRate != null) {
            return Map.of(
                    "current", current,
                    "change", change,
                    "changeRate", changeRate
            );
        } else {
            return null;
        }
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

    private StockResponseDto makeStockResponseDto(Map<String, String> responseMap, String order) {
        String stockCode = "change".equals(order) ? CHANGE_STOCK_CODE : STOCK_CODE;

        Stock stock = stockRepository.findByCode(responseMap.get(stockCode)).orElseThrow(StockNotFoundException::new);

        return StockResponseDto.builder()
                .name(responseMap.get(STOCK_NAME))
                .code(responseMap.get(stockCode))
                .logo(stock.getLogo())
                .price(Integer.parseInt(responseMap.get(STOCK_CURRENT_PRICE)))
                .change(Integer.parseInt(responseMap.get(CHANGE_FROM_PREVIOUS_STOCK)))
                .changeRate(Double.parseDouble(responseMap.get(CHANGE_RATE_FROM_PREVIOUS_STOCK)))
                .build();
    }

    // 16. 스껄계산기
    public SkrrrCalculatorResponseDto calculateSkrrr(String code, SkrrrCalculatorRequestDto requestDto) {

        String date = requestDto.getDate();
        DateTimeFormatter originFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate originDate = LocalDate.parse(date, originFormatter);
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String startDate = originDate.minusDays(5).format(newFormatter);
        String endDate = originDate.format(newFormatter);

        // 주식 상장일자 체크
        String publicParams = "PRDT_TYPE_CD=300" +
                "&PDNO=" + code;

        Map publicResponse = restClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/search-stock-info" + publicParams)
                .headers(setRequestHeaders("CTPF1002R"))
                .retrieve()
                .body(Map.class);

        Map<String, String> publicOutput = (Map<String, String>) publicResponse.get("output");

        String publicDate = publicOutput.get("scts_mket_lstg_dt");
        String privateDate = publicOutput.get("scts_mket_lstg_abol_dt");

        if (publicDate.compareTo(startDate) > 0 || privateDate.compareTo(endDate) < 0) {
            throw new StockNotPublicException();
        }

        // 과거 주식시세 요청
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + code +
                "&fid_input_date_1=" + startDate +
                "&fid_input_date_2=" + endDate +
                "&fid_period_div_code=D" +
                "&fid_org_adj_prc=1";

        Map response = restClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice" + queryParams)
                .headers(setRequestHeaders("FHKST03010100"))
                .retrieve()
                .body(Map.class);

        List<Map<String, String>> output = (List<Map<String, String>>) response.get("output2");

        String close = "stck_clpr";
        String previous = output.get(0).get(close);

        if (previous == null) {
            for (Map<String, String> map : output) {
                if (map.get(close) != null)
                    previous = map.get(close);
            }
        }

        if (previous == null) {
            throw new StockNotPublicException();
        }

        // 현재가 기반 계산
        Map<String, String> redisMap = getCurrentChangeChangeRate(code);
        if(redisMap == null) {
            throw new CurrentNotFoundException();
        }
        int currentPrice = Integer.parseInt(redisMap.get(REDIS_CURRENT_KEY));
        int previousPrice = Integer.parseInt(previous);
        int quantity = (int) (requestDto.getPrice() / currentPrice);

        long ret = (long) (currentPrice - previousPrice) * quantity;

        int candy = 500;
        int soul = 9_000;
        int chicken = 23_000;
        int iphone = 1_400_000;
        int slave = 9_860;

        return SkrrrCalculatorResponseDto.builder()
                .price(ret)
                .candy(String.valueOf(ret / candy))
                .soul(String.valueOf(ret / soul))
                .chicken(String.valueOf(ret / chicken))
                .iphone(String.valueOf(ret / iphone))
                .slave(String.valueOf(ret / slave))
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
