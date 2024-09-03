package com.hrth.ustock.controller.common;

import com.hrth.ustock.service.cron.StockCronService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@Profile("prod")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/scheduler")
@EnableScheduling
public class CronController {
    private final StockCronService stockCronService;

//    // 주중 오전 9시에 시작해서 30분마다 실행하고 오후 16시 15분에 끝남
//    // 현재가, 등락, 등락율, 날짜를 redis에 저장
//    @Scheduled(cron = "0 */15 8-16 * * MON-SAT")
//    public void setChartData() {
//        stockCronService.saveStockData();
//    }
//
//    // 5분마다 코스피, 코스닥 데이터 받아옴
//    @Scheduled(cron = "0 */5 * * * *")
//    public void setMarketData() {
//        stockCronService.saveMarketData();
//    }
//
//    // 화-토 오전 9시 10분에 전일(수정주가) 차트 데이터 mysql에 저장
//    @Scheduled(cron = "0 10 09 ? * TUE-SAT")
//    public void setEditedChartData() {
//        stockCronService.saveEditedChartData();
//    }

    @GetMapping("/test/3")
    public void setEditedChartDataTest() {
        stockCronService.saveEditedChartData();
    }
}
