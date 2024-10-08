package com.hrth.ustock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    @Value("${spring.config.url}")
    private String url;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowedMethods("*")
                .allowedOrigins(url)
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600L);
    }
}