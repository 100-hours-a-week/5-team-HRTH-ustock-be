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
import java.io.PrintWriter;

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

        if (refresh == null || jwtUtil.isExpired(refresh) || !jwtUtil.getCategory(refresh).equals("refresh")) {
            log.info("logout: refresh not valid, url: {}", request.getRequestURL());
            PrintWriter writer = response.getWriter();
            writer.print("logout: refresh not valid");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //DB에 저장되어 있는지 확인
        Long userId = jwtUtil.getUserId(refresh);
        if (userId == null) {
            log.info("logout: cannot get userId, url: {}", request.getRequestURL());
            PrintWriter writer = response.getWriter();
            writer.print("logout: cannot get userId");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String currentRefresh = (String) redisTemplate.opsForValue().get("RT:" + userId);
        if (currentRefresh == null) {
            log.info("logout: cannot find cache, url: {}", request.getRequestURL());
            PrintWriter writer = response.getWriter();
            writer.print("logout: cannot find cache");

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!currentRefresh.equals(refresh)) {
            log.info("logout: refresh not match, url: {}", request.getRequestURL());
            PrintWriter writer = response.getWriter();
            writer.print("logout: refresh not match");

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        //로그아웃 진행
        redisTemplate.delete(currentRefresh);

        //Access, Refresh 토큰 Cookie 값 0 세팅 후 클라이언트 측 쿠키 갱신
        Cookie accessLogout = new Cookie("access", null);
        accessLogout.setMaxAge(0);
        accessLogout.setPath("/");
        accessLogout.setHttpOnly(true);
        accessLogout.setSecure(true);
        accessLogout.setDomain(domain);

        Cookie refreshLogout = new Cookie("refresh", null);
        refreshLogout.setMaxAge(0);
        refreshLogout.setPath("/");
        refreshLogout.setHttpOnly(true);
        refreshLogout.setSecure(true);
        refreshLogout.setDomain(domain);

        response.addCookie(accessLogout);
        response.addCookie(refreshLogout);
        response.setStatus(HttpStatus.OK.value());
    }
}
