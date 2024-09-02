package com.hrth.ustock.service.cron;

import com.hrth.ustock.dto.main.stock.MarketResponseDto;
import com.hrth.ustock.entity.main.Chart;
import com.hrth.ustock.entity.main.Stock;
import com.hrth.ustock.exception.kisapi.KisApiException;
import com.hrth.ustock.repository.main.ChartBatchRepository;
import com.hrth.ustock.repository.main.StockRepository;
import com.hrth.ustock.util.KisApiAuthManager;
import com.hrth.ustock.util.RedisJsonManager;
import com.hrth.ustock.util.TimeDelay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hrth.ustock.exception.kisapi.KisApiExceptionType.API_REQUEST_FAILED;
import static com.hrth.ustock.service.main.StockServiceConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCronService {

    private final StockRepository stockRepository;
    private final ChartBatchRepository chartBatchRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KisApiAuthManager authManager;

    private static final DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter redisFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm");
    private static final DateTimeFormatter mysqlFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final RedisJsonManager redisJsonManager;

    // 주중 오전 9시에 시작해서 30분마다 실행하고 오후 15시 30분에 끝남
    // 종목별 현재가, 차트 데이터 redis에 갱신
    public void saveStockData() {
        log.info("현재가 크론잡 시작");
        String startDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(requestFormatter);
        String redisDate = minuteFormatter();
        List<Stock> allStocks = stockRepository.findAll();

        for (Stock stock : allStocks) {
            String code = stock.getCode();
            String queryParams = "?fid_cond_mrkt_div_code=J" +
                    "&fid_input_iscd=" + code +
                    "&fid_input_date_1=" + startDate +
                    "&fid_input_date_2=" + startDate +
                    "&fid_period_div_code=D" +
                    "&fid_org_adj_prc=0";
            String apiUri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

            Map response = authManager.getApiData(apiUri, queryParams, "FHKST03010100");

            if (response == null) {
                continue;
            }

            Map<String, String> output1 = (Map<String, String>) response.get("output1");

            if (output1 == null || output1.isEmpty()) {
                log.info("saveStockChartData: api request failed, code: {}", code);
                continue;
            }

            // stock data
            String current = output1.get(STOCK_CURRENT_PRICE);
            String change = output1.get(CHANGE_FROM_PREVIOUS_STOCK);
            String changeRate = output1.get(CHANGE_RATE_FROM_PREVIOUS_STOCK);

            redisTemplate.opsForHash().put(code, REDIS_CURRENT_KEY, current);
            redisTemplate.opsForHash().put(code, REDIS_CHANGE_KEY, change);
            redisTemplate.opsForHash().put(code, REDIS_CHANGE_RATE_KEY, changeRate);
            redisTemplate.opsForHash().put(code, REDIS_DATE_KEY, redisDate);
//
//            List<Map<String, String>> output2 = (List<Map<String, String>>) response.get("output2");
//            // 분봉
//            // redis에 json String으로 저장함
//            String high = output1.get(STOCK_MARKET_HIGH);
//            String low = output1.get(STOCK_MARKET_LOW);
//            String open = output1.get(STOCK_MARKET_OPEN);
//            // 장 마감 전까진 종가가 없으니 현재가로 저장
//            String close = output1.get(STOCK_MARKET_CLOSE);
//
//            List<Map<String, String>> charts;
//            String result = (String) redisTemplate.opsForHash().get(code, REDIS_CHART_KEY);
//
//            charts = result == null ? new ArrayList<>() : redisJsonManager.stringJsonConvert(result);
//
//            Map<String, String> chart = new HashMap<>();
//            chart.put(REDIS_CHART_HIGH_KEY, high);
//            chart.put(REDIS_CHART_LOW_KEY, low);
//            chart.put(REDIS_CHART_OPEN_KEY, open);
//            chart.put(REDIS_CHART_CLOSE_KEY, close);
//            chart.put(REDIS_CHART_DATE_KEY, redisDate);
//            charts.add(chart);
//
//            String jsonString = redisJsonManager.jsonStringConvert(charts);
//            redisTemplate.opsForHash().put(code, REDIS_CHART_KEY, jsonString);

            TimeDelay.delay(200);
        }
        log.info("현재가 크론잡 종료");
    }

    public void saveMarketData() {
        Map<String, String> kospi = requestMarketInfo(KOSPI_CODE);
        Map<String, String> kosdaq = requestMarketInfo(KOSDAQ_CODE);

        String redisDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(redisFormatter);

        if (kospi == null || kosdaq == null) {
            return;
        }

        Map<String, Object> marketInfo = new HashMap<>();
        marketInfo.put("kospi", makeMarketDto(kospi));
        marketInfo.put("kosdaq", makeMarketDto(kosdaq));
        marketInfo.put("date", redisDate);

        String jsonString = redisJsonManager.mapStringConvert(marketInfo);
        redisTemplate.opsForValue().set("market_info", jsonString);
    }

    private Map<String, String> requestMarketInfo(String marketCode) {
        String queryParams = "?fid_cond_mrkt_div_code=U"
                + "&fid_input_iscd=" + marketCode;
        String apiUri = "/uapi/domestic-stock/v1/quotations/inquire-index-price";

        Map response = authManager.getApiData(apiUri, queryParams, "FHPUP02100000");

        if (response == null) {
            return null;
        }

        if (response.get("output") == null || response.get("output").equals("")) {
            log.info("requestMarketInfo: api request failed, code: {}", marketCode);
            throw new KisApiException(API_REQUEST_FAILED);
        }

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

    @Transactional
    public void saveEditedChartData() {
        log.info("차트 크론잡 시작");
        String startDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1).format(requestFormatter);
        String mysqlDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1).format(mysqlFormatter);

        List<Stock> allStocks = stockRepository.findAll();
        List<Chart> chartList = new ArrayList<>();
        for (Stock stock : allStocks) {
            String code = stock.getCode();
            // 장이 열리면 전날 redis_chart 삭제
            redisTemplate.opsForHash().delete(code, REDIS_CHART_KEY);
            String queryParams = "?fid_cond_mrkt_div_code=J" +
                    "&fid_input_iscd=" + code +
                    "&fid_input_date_1=" + startDate +
                    "&fid_input_date_2=" + startDate +
                    "&fid_period_div_code=D" +
                    "&fid_org_adj_prc=0";
            String apiUri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

            Map response = authManager.getApiData(apiUri, queryParams, "FHKST03010100");

            if (response == null) continue;

            List<Map<String, String>> output2 = (List<Map<String, String>>) response.get("output2");

            if (output2 == null || output2.isEmpty() || output2.get(0).isEmpty()) {
                continue;
            }

            // chart data
            String high = output2.get(0).get(STOCK_HIGH);
            String low = output2.get(0).get(STOCK_LOW);
            String open = output2.get(0).get(STOCK_OPEN);
            String close = output2.get(0).get(STOCK_CLOSE);

            Chart chart = Chart.builder()
                    .stock(stock)
                    .high(Integer.parseInt(high))
                    .low(Integer.parseInt(low))
                    .open(Integer.parseInt(open))
                    .close(Integer.parseInt(close))
                    .date(mysqlDate)
                    .build();

            chartList.add(chart);

            TimeDelay.delay(100);
        }
        chartBatchRepository.batchInsert(chartList);
        log.info("차트 크론잡 종료");
    }

    private String minuteFormatter() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 현재 분을 00분 또는 30분으로 맞춤
        int minute = now.getMinute();
        if (minute >= 30) {
            now = now.withMinute(30);
        } else {
            now = now.withMinute(0);
        }
        return now.format(redisFormatter);
    }
}
