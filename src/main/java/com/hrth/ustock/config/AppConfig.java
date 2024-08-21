package com.hrth.ustock.config;

import com.hrth.ustock.util.DateConverter;
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
    public DateConverter dateconverter() {
        return new DateConverter();
    }

}
