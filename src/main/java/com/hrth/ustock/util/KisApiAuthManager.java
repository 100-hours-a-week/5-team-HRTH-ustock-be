package com.hrth.ustock.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class KisApiAuthManager {

    public static final int MAX_TRY = 5;
    @Getter
    private final String appKey;
    @Getter
    private final String appSecret;
    private final RestClient restClient;
    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public KisApiAuthManager(@Value("${kis.appkey}") String appKey,
                             @Value("${kis.appsecret}") String appSecret,
                             RedisTemplate<String, String> redisTemplate) {

        this.appKey = appKey;
        this.appSecret = appSecret;
        this.redisTemplate = redisTemplate;

        this.restClient = RestClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .build();
    }

    public Map getApiData(String uri, String queryParams, String header) {
        for (int i = 0; i < MAX_TRY; i++) {
            Map response = restClient.get()
                    .uri(uri + queryParams)
                    .headers(setRequestHeaders(header))
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("msg1").equals("기간이 만료된 token 입니다.")) {
                generateToken();
            } else if (response.get("msg1").equals("초당 거래건수를 초과하였습니다.")) {
                TimeDelay.delay(1000);
            } else if (response.get("error_description").equals("접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)")) {
                TimeDelay.delay(60 * 1000);
            } else {
                return response;
            }
        }
        return null;
    }

    private Consumer<HttpHeaders> setRequestHeaders(String trId) {
        return httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setBearerAuth(getToken());
            httpHeaders.set("appkey", getAppKey());
            httpHeaders.set("appsecret", getAppSecret());
            httpHeaders.set("tr_id", trId);
            httpHeaders.set("custtype", "P");
        };
    }

    public String getToken() {
        String findToken = redisTemplate.opsForValue().get("ACCESS_TOKEN");
        if (findToken != null) return findToken;
        else return generateToken();
    }

    public String generateToken() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("appkey", appKey);
        requestBody.put("appsecret", appSecret);

        Map response = restClient.post()
                .uri("/oauth2/tokenP")
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        String token = (String) response.get("access_token");
        String tokenExpired = (String) response.get("access_token_token_expired");

        ZonedDateTime tokenExpireDate = LocalDateTime.parse(tokenExpired, requestFormatter).atZone(ZoneId.of("Asia/Seoul"));
        long expireSecond = RedisTTLCalculator.calculateTTLForMidnightKST(tokenExpireDate);

        redisTemplate.opsForValue().set("ACCESS_TOKEN", token);
        redisTemplate.expire("ACCESS_TOKEN", expireSecond, TimeUnit.SECONDS);

        log.info("Token Created: {}", token);
        return token;
    }
}
