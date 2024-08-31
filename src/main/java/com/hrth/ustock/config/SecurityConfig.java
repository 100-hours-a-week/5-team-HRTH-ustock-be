package com.hrth.ustock.config;

import com.hrth.ustock.jwt.CustomLogoutFilter;
import com.hrth.ustock.jwt.JWTFilter;
import com.hrth.ustock.jwt.JWTUtil;
import com.hrth.ustock.oauth2.CustomSuccessHandler;
import com.hrth.ustock.oauth2.OAuth2FailureHandler;
import com.hrth.ustock.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.config.url}")
    private String url;
    @Value("${spring.config.domain}")
    private String domain;

    private final JWTUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2FailureHandler oauth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // from 로그인 방식 disable
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 방식 disable
                .httpBasic(AbstractHttpConfigurer::disable)

                // 로그인 무한루프 방지
                .addFilterAfter(new JWTFilter(domain, jwtUtil, redisTemplate), OAuth2LoginAuthenticationFilter.class)

                // OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customSuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig.userService(customOAuth2UserService))
                )

                // csrf 설정 - post 요청 + 로그아웃용
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
//                .csrf(csrf -> csrf
//                        .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler())
//                )

                // 로그아웃 필터
                .addFilterBefore(new CustomLogoutFilter(domain, jwtUtil, redisTemplate), LogoutFilter.class)

                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/logout", "/v1/portfolio/**", "/v1/portfolio", "/v1/user").authenticated()
                        .anyRequest().permitAll())
                // 경로별 인가 작업 - 개발중 테스트용 /**,
//                .authorizeHttpRequests((auth) -> auth
//                        .requestMatchers("/**").permitAll()
//                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 설정 : STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        return http.build();
    }
}
