package com.hrth.ustock.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final String domain;
    private final String url;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {


        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }
        Cookie accessLogout = null;
        Cookie refreshLogout = null;
        if (refresh == null || jwtUtil.isExpired(refresh) || jwtUtil.getUserId(refresh) == null) {
            accessLogout = setLogoutCookie("access");
            refreshLogout = setLogoutCookie("refresh");

            response.addCookie(accessLogout);
            response.addCookie(refreshLogout);
            response.setStatus(HttpStatus.OK.value());
            return;
        }

        Long userId = jwtUtil.getUserId(refresh);
        String currentRefresh = (String) redisTemplate.opsForValue().get("RT:" + userId);
        if (currentRefresh != null && currentRefresh.equals(refresh)) {
            redisTemplate.delete(currentRefresh);
        }

        accessLogout = setLogoutCookie("access");
        refreshLogout = setLogoutCookie("refresh");

        response.addCookie(accessLogout);
        response.addCookie(refreshLogout);
        response.setStatus(HttpStatus.OK.value());
    }

    private Cookie setLogoutCookie(String str) {
        Cookie cookie = new Cookie(str, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain(domain);
        return cookie;
    }
}
