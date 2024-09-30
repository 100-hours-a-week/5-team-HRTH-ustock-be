package com.hrth.ustock.util;

import com.hrth.ustock.exception.kisapi.KisApiException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.hrth.ustock.exception.kisapi.KisApiExceptionType.API_REQUEST_FAILED;

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
    private final RateLimiter cronRateLimiter;
    private final RateLimiter userRateLimiter;


    public KisApiAuthManager(@Value("${kis.appkey}") String appKey,
                             @Value("${kis.appsecret}") String appSecret,
                             RedisTemplate<String, String> redisTemplate,
                             RestClient restClient,
                             RateLimiter cronRateLimiter,
                             RateLimiter userRateLimiter) {

        this.appKey = appKey;
        this.appSecret = appSecret;
        this.redisTemplate = redisTemplate;
        this.restClient = restClient;
        this.cronRateLimiter = cronRateLimiter;
        this.userRateLimiter = userRateLimiter;
    }

    public Map getUserData(String uri, String queryParams, String header) {
        return getApiData(uri, queryParams, header, userRateLimiter);
    }

    public Map getCronData(String uri, String queryParams, String header) {
        return getApiData(uri, queryParams, header, cronRateLimiter);
    }

    private Map getApiData(String uri, String queryParams, String header, RateLimiter rateLimiter) {
        for (int i = 0; i < MAX_TRY; i++) {
            Map response;
            try {
                response = rateLimiter.executeSupplier(() -> restClient.get()
                        .uri(uri + queryParams)
                        .headers(setRequestHeaders(header))
                        .retrieve()
                        .body(Map.class));
            } catch (HttpClientErrorException e) {
                throw new KisApiException(API_REQUEST_FAILED);
            }

            if (response.get("msg1") == null) {
                throw new KisApiException(API_REQUEST_FAILED);
            }
            String msg1 = (String) response.get("msg1");
            if ("기간이 만료된 token 입니다.".equals(msg1)) {
                generateToken();
            } else if ("초당 거래건수를 초과하였습니다.".equals(msg1)) {
                TimeDelay.delay(1000);
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

    private String getToken() {
        String findToken = redisTemplate.opsForValue().get("ACCESS_TOKEN");
        if (findToken != null) {
            return findToken;
        }

        return generateToken();
    }

    private String generateToken() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("appkey", appKey);
        requestBody.put("appsecret", appSecret);

        Map response = cronRateLimiter.executeSupplier(() -> restClient.post()
                .uri("/oauth2/tokenP")
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(requestBody)
                .retrieve()
                .body(Map.class));

        String token = (String) response.get("access_token");
        String tokenExpired = (String) response.get("access_token_token_expired");

        ZonedDateTime tokenExpireDate = LocalDateTime.parse(tokenExpired, requestFormatter).atZone(ZoneId.of("Asia/Seoul"));
        long expireSecond = calculateTTLForMidnightKST(tokenExpireDate);
        redisTemplate.opsForValue().set("ACCESS_TOKEN", token);
        log.info("Stored ACCESS_TOKEN in Redis: {}, TTL: {}h", token, expireSecond / 3600);
        redisTemplate.expire("ACCESS_TOKEN", expireSecond, TimeUnit.SECONDS);
        return token;
    }

    private static long calculateTTLForMidnightKST(ZonedDateTime zonedDateTime) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Duration duration = Duration.between(now, zonedDateTime);
        return duration.getSeconds();
    }
}
