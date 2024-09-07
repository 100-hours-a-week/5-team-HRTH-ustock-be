package com.hrth.ustock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.util.DateConverter;
import com.hrth.ustock.util.RedisJsonManager;
import com.hrth.ustock.util.RedisTTLCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .build();
    }

    @Bean
    public DateConverter dateConverter() {
        return new DateConverter();
    }

    @Bean
    public RedisTTLCalculator redisTTLCalculator() {
        return new RedisTTLCalculator();
    }

    @Bean
    public RedisJsonManager redisJsonManager() {
        return new RedisJsonManager(new ObjectMapper());
    }
}
