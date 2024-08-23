package com.hrth.ustock.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class KisApiAuthManager {

    @Getter
    private final String appKey;
    @Getter
    private final String appSecret;
    private final RestClient restClient;
    private final RedisTemplate<String, String> redisTemplate;


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

    public String generateToken() {
        String findToken = redisTemplate.opsForValue().get("ACCESS_TOKEN");
        String findTokenExpire = redisTemplate.opsForValue().get("ACCESS_TOKEN_EXPIRE");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime expireDate = LocalDateTime.parse(findTokenExpire, formatter);
        LocalDateTime now = LocalDateTime.now();

        log.info("expire date: {}, now: {}", expireDate, now);

        if (!expireDate.isBefore(now)) return findToken;

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

        redisTemplate.opsForValue().set("ACCESS_TOKEN", token);
        redisTemplate.opsForValue().set("ACCESS_TOKEN_EXPIRE", tokenExpired);

        log.info("Token Created: {}", token);
        return token;
    }
}
