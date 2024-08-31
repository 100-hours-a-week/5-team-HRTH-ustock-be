package com.hrth.ustock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.dto.chart.ChartDto;
import com.hrth.ustock.dto.chart.ChartResponseDto;
import com.hrth.ustock.dto.stock.*;
import com.hrth.ustock.entity.portfolio.Chart;
import com.hrth.ustock.entity.portfolio.News;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.domain.chart.ChartException;
import com.hrth.ustock.exception.domain.stock.StockException;
import com.hrth.ustock.repository.ChartRepository;
import com.hrth.ustock.repository.NewsRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.service.cron.StockCronService;
import com.hrth.ustock.util.DateConverter;
import com.hrth.ustock.util.KisApiAuthManager;
import com.hrth.ustock.util.RedisJsonManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hrth.ustock.exception.domain.chart.ChartExceptionType.CHART_NOT_FOUND;
import static com.hrth.ustock.exception.domain.chart.ChartExceptionType.PERIOD_NOT_ALLOWED;
import static com.hrth.ustock.exception.domain.stock.StockExceptionType.*;
import static com.hrth.ustock.service.StockServiceConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private static final int TOP_RANK_RANGE = 5;

    private final NewsRepository newsRepository;
    private final StockRepository stockRepository;
    private final ChartRepository chartRepository;
    private final StockCronService stockCronService;
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;
    private final DateConverter dateConverter;
    private final KisApiAuthManager authManager;
    private final RedisJsonManager redisJsonManager;

    private static final DateTimeFormatter redisFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm");

    // 6. 종목 검색
    @Transactional
    public List<StockResponseDto> searchStock(String query) {
        String stockCodeRegex = "^\\d{1,6}$|^Q\\d{1,6}$";

        List<Stock> list;
        if (query.matches(stockCodeRegex)) {
            list = stockRepository.findByCodeStartingWith(query);
            list.addAll(stockRepository.findByCodeContainingButNotStartingWith(query));
        } else {
            list = stockRepository.findByNameStartingWith(query);
            list.addAll(stockRepository.findByNameContainingButNotStartingWith(query));
        }

        if (list.isEmpty())
            throw new StockException(STOCK_NOT_FOUND);

        List<StockResponseDto> stockDtoList = new ArrayList<>();
        for (Stock stock : list) {
            Map<String, String> redisMap = getCurrentChangeChangeRate(stock.getCode());

            if (redisMap == null) continue;

            String currentSaved = redisMap.get(REDIS_CURRENT_KEY);
            String rateSaved = redisMap.get(REDIS_CHANGE_RATE_KEY);

            StockResponseDto stockDto = stock.toDto();
            stockDto.setPrice(Integer.parseInt(currentSaved));
            stockDto.setChangeRate(Double.parseDouble(rateSaved));
            stockDtoList.add(stockDto);
        }

        return stockDtoList;
    }

    // 14. 주식 상세정보 조회
    @Transactional
    public StockResponseDto getStockInfo(String code) {
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new StockException(STOCK_NOT_FOUND));

        Map<String, String> redisMap = getCurrentChangeChangeRate(stock.getCode());

        if (redisMap == null)
            throw new StockException(CURRENT_NOT_FOUND);

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
        if (period < 1 || 3 < period)
            throw new ChartException(PERIOD_NOT_ALLOWED);

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

        if (chartList == null || chartList.isEmpty())
            throw new ChartException(CHART_NOT_FOUND);

        for (Pair<String, String> data : dateList) {
            String startDate = data.getFirst();
            String endDate = data.getSecond();

            ChartResponseDto chartResponseDto = new ChartResponseDto();
            chartResponseDto.setCandle(ChartDto.builder().build());
            chartResponseDto.setNews(new ArrayList<>());

            ChartDto candle = chartResponseDto.getCandle();
            candle.setHigh(Integer.MIN_VALUE);
            candle.setLow(Integer.MAX_VALUE);
            chartResponseDto.setDate(startDate);

            chartList.stream()
                    .filter(chart ->
                    {
                        String date = chart.getDate();
                        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
                    })
                    .forEach(chart -> {
                        if (chart.getDate().equals(startDate)) {
                            candle.setOpen(chart.getOpen());
                        }
                        candle.setClose(chart.getClose());
                        candle.setHigh(Math.max(chart.getHigh(), candle.getHigh()));
                        candle.setLow(Math.min(chart.getLow(), candle.getLow()));
                    });

            if (candle.getHigh() == Integer.MIN_VALUE) continue;

            // 시가 0으로 반환하지 않도록 수정
            if (candle.getOpen() == 0) {
                candle.setOpen(chartList.get(0).getOpen());
            }

            newsList.stream()
                    .filter(news ->
                    {
                        String date = news.getDate();
                        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
                    })
                    .forEach(news -> chartResponseDto.getNews().add(news.toEmbedDto()));

            chartListResponse.add(chartResponseDto);
        }

        return chartListResponse;
    }

    public AllMarkterResponseDto getMarketInfo() {
        String marketInfo = redisTemplate.opsForValue().get("market_info");

        log.info(marketInfo);
        if (marketInfo == null) {
            stockCronService.saveMarketData();
            marketInfo = redisTemplate.opsForValue().get("market_info");
        }

        Map<String, Object> redisResult = redisJsonManager.stringMapConvert(marketInfo);
        log.info("redisResult: {}", redisResult);

        MarketResponseDto kospi = objectMapper.convertValue(redisResult.get("kospi"), MarketResponseDto.class);
        MarketResponseDto kosdaq = objectMapper.convertValue(redisResult.get("kosdaq"), MarketResponseDto.class);

        return AllMarkterResponseDto.builder()
                .kospi(kospi)
                .kosdaq(kosdaq)
                .build();

    }

    public List<StockResponseDto> getStockList(String order) {
        return switch (order) {
            case "top", "trade" -> requestOrderByTrade(order);
            case "capital" -> requestOrderByCapital();
            case "change" -> requestOrderByChange();
            default -> throw new IllegalArgumentException();
        };
    }

    // 현재가, 전일대비, 전일 대비 부호, 전일 대비율 조회
    protected Map<String, String> getCurrentChangeChangeRate(String code) {
        String current = (String) redisTemplate.opsForHash().get(code, REDIS_CURRENT_KEY);
        String change = (String) redisTemplate.opsForHash().get(code, REDIS_CHANGE_KEY);
        String changeRate = (String) redisTemplate.opsForHash().get(code, REDIS_CHANGE_RATE_KEY);

        if (current == null || change == null || changeRate == null)
            throw new StockException(CURRENT_NOT_FOUND);

        return Map.of(
                "current", current,
                "change", change,
                "changeRate", changeRate
        );
    }

    private List<StockResponseDto> requestOrderByTrade(String order) {
        String redis_key = order.equals(REDIS_ORDER_TOP) ? REDIS_ORDER_TOP : REDIS_ORDER_TRADE;
        String redisResult = redisTemplate.opsForValue().get("ranking_" + redis_key + "_" + minuteFormatter());

        if (redisResult != null)
            return redisJsonManager.stringDtoConvert(redisResult);

        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20171" +
                "&fid_div_cls_code=1" +
                "&fid_input_iscd=0000" +
                "&fid_trgt_cls_code=0" +
                "&fid_blng_cls_code=0" +
                "&fid_trgt_cls_code=111111111" +
                "&fid_trgt_exls_cls_code=0111001101" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_input_date_1=";

        String apiUri = "/uapi/domestic-stock/v1/quotations/volume-rank";
        Map response = authManager.getApiData(apiUri, queryParams, "FHPST01710000");

        return saveToRedis(response, redis_key);
    }

    private List<StockResponseDto> requestOrderByCapital() {
        String redisResult = redisTemplate.opsForValue().get("ranking_" + "capital_" + minuteFormatter());

        if (redisResult != null)
            return redisJsonManager.stringDtoConvert(redisResult);

        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20174" +
                "&fid_div_cls_code=1" +
                "&fid_input_iscd=0000" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=";

        String apiUri = "/uapi/domestic-stock/v1/ranking/market-cap";
        Map response = authManager.getApiData(apiUri, queryParams, "FHPST01740000");

        return saveToRedis(response, "capital");
    }

    private List<StockResponseDto> requestOrderByChange() {
        String redisResult = redisTemplate.opsForValue().get("ranking_" + "change_" + minuteFormatter());

        if (redisResult != null)
            return redisJsonManager.stringDtoConvert(redisResult);

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

        String apiUri = "/uapi/domestic-stock/v1/ranking/fluctuation";
        Map response = authManager.getApiData(apiUri, queryParams, "FHPST01700000");

        return saveToRedis(response, "change");
    }

    private List<StockResponseDto> saveToRedis(Map response, String redis_key) {
        if (response == null || response.get("output") == null || response.get("output").equals(""))
            throw new StockException(STOCK_REQUEST_FAILED);

        List<Map<String, String>> output = (List<Map<String, String>>) response.get("output");

        List<StockResponseDto> stockList = makeStockResponseDto(output, redis_key);

        String dtoString = redisJsonManager.dtoStringConvert(stockList);
        redisTemplate.opsForValue().set("ranking_" + redis_key + "_" + minuteFormatter(), dtoString);

        int fortyMinute = 40 * 60;
        redisTemplate.expire("ranking_" + redis_key + "_" + minuteFormatter(), fortyMinute, TimeUnit.SECONDS);

        return stockList;
    }

    private String minuteFormatter() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 현재 분을 00분 또는 30분으로 맞춤
        int minute = now.getMinute();
        now = minute >= 30 ? now.withMinute(30) : now.withMinute(0);

        return now.format(redisFormatter);
    }

    private List<StockResponseDto> makeStockResponseDto(List<Map<String, String>> responseList, String order) {
        int range = "top".equals(order) ? TOP_RANK_RANGE : responseList.size();
        String stockCodeKey = "change".equals(order) ? CHANGE_STOCK_CODE : STOCK_CODE;

        List<StockResponseDto> stockList = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            Map<String, String> responseMap = responseList.get(i);

            stockList.add(StockResponseDto.builder()
                    .name(responseMap.get(STOCK_NAME))
                    .code(responseMap.get(stockCodeKey))
                    .price(Integer.parseInt(responseMap.get(STOCK_CURRENT_PRICE)))
                    .change(Integer.parseInt(responseMap.get(CHANGE_FROM_PREVIOUS_STOCK)))
                    .changeRate(Double.parseDouble(responseMap.get(CHANGE_RATE_FROM_PREVIOUS_STOCK)))
                    .build()
            );
        }

        List<String> codeList = stockList.stream()
                .map(StockResponseDto::getCode)
                .toList();

        List<Stock> findStockList = stockRepository.findAllByCodeIn(codeList);
        for (Stock stock : findStockList) {
            for (StockResponseDto dto : stockList) {
                if (dto.getCode().equals(stock.getCode())) {
                    dto.setLogo(stock.getLogo());
                    break;
                }
            }
        }

        for (StockResponseDto stock : stockList) {
            if (!codeList.contains(stock.getCode())) {
                stockRepository.save(
                        Stock.builder()
                                .name(stock.getName())
                                .code(stock.getCode())
                                .build()
                );
            }
        }

        return stockList;
    }

    public SkrrrCalculatorResponseDto calculateSkrrr(String code, SkrrrCalculatorRequestDto requestDto) {
        String date = requestDto.getDate();

        if (!isValidDate(date))
            throw new StockException(CALCULATOR_DATE_INVALID);

        long calculatorMaxPrice = 9_999_999_999_999L;
        if (requestDto.getPrice() > calculatorMaxPrice)
            throw new StockException(CALCULATOR_PRICE_INVALID);

        DateTimeFormatter originFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate originDate = LocalDate.parse(date, originFormatter);
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String startDate = originDate.minusDays(5).format(newFormatter);
        String endDate = originDate.format(newFormatter);

        String publicParams = "?PRDT_TYPE_CD=30090-80&PDNO=" + code;

        String publicUri = "/uapi/domestic-stock/v1/quotations/search-stock-info";
        Map publicResponse = authManager.getApiData(publicUri, publicParams, "CTPF1002R");

        Map<String, String> publicOutput = (Map<String, String>) publicResponse.get("output");

        String publicDate = publicOutput.get("scts_mket_lstg_dt");
        String privateDate = publicOutput.get("scts_mket_lstg_abol_dt");

        if (publicDate.compareTo(startDate) > 0 || !"".equals(privateDate))
            throw new StockException(STOCK_NOT_PUBLIC);

        // 과거 주식시세 요청
        String queryParams = "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + code +
                "&fid_input_date_1=" + startDate +
                "&fid_input_date_2=" + endDate +
                "&fid_period_div_code=D" +
                "&fid_org_adj_prc=0";

        String apiUri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        Map response = authManager.getApiData(apiUri, queryParams, "FHKST03010100");

        List<Map<String, String>> output = (List<Map<String, String>>) response.get("output2");

        String close = "stck_clpr";
        String previous = output.get(0).get(close);

        if (previous == null) {
            for (Map<String, String> map : output) {
                if (map.get(close) != null)
                    previous = map.get(close);
            }
        }

        if (previous == null)
            throw new StockException(STOCK_NOT_PUBLIC);

        if (requestDto.getPrice() < Integer.parseInt(previous))
            throw new StockException(STOCK_CANNOT_PURCHASE);

        // 현재가 기반 계산
        Map<String, String> redisMap = getCurrentChangeChangeRate(code);
        if (redisMap == null)
            throw new StockException(CURRENT_NOT_FOUND);

        int currentPrice = Integer.parseInt(redisMap.get(REDIS_CURRENT_KEY));
        int previousPrice = Integer.parseInt(previous);
        long quantity = requestDto.getPrice() / currentPrice;

        long ret = (currentPrice - previousPrice) * quantity;

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

    private boolean isValidDate(String date) {
        String[] dateInput = date.split("/");
        int year = Integer.parseInt(dateInput[0]);
        int month = Integer.parseInt(dateInput[1]);
        int day = Integer.parseInt(dateInput[2]);

        boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        int february = isLeapYear ? 29 : 28;

        int[] limitDate = {0, 31, february, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        return limitDate[month] >= day;
    }
}