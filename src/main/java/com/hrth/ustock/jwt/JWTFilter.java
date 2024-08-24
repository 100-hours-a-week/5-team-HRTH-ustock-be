package com.hrth.ustock.jwt;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserOauthDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    public static final long ACCESS_EXPIRE = 600000L;
    public static final long REFRESH_EXPIRE = 86400000L;
    public static final int COOKIE_EXPIRE = 360000;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String access = null;
        String refresh = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("access")) {
                access = cookie.getValue();
            }
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if ((access == null || access.isEmpty()) && (refresh == null || refresh.isEmpty())) {
            filterChain.doFilter(request, response);
            return;
        }

        // access token 소멸시간 검증
        if (!(access == null || access.isEmpty()) && !jwtUtil.isExpired(access)) {
            String category = jwtUtil.getCategory(access);

            // 토큰의 카테고리가 access가 아닌 경우 UNAUTHORIZED return
            // 해결방법은 access token Application에서 수동 삭제
            if (!category.equals("access")) {
                PrintWriter writer = response.getWriter();
                writer.print("invalid access token");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // Access Token이 만료된 경우 Refresh Token으로 재발급
            if (refresh == null || jwtUtil.isExpired(refresh) || !jwtUtil.getCategory(refresh).equals("refresh")) {
                // Refresh Token이 유효하지 않은 경우
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long userId = jwtUtil.getUserId(refresh);
            String provider = jwtUtil.getProvider(refresh);
            String providerId = jwtUtil.getProviderId(refresh);
            String providerName = jwtUtil.getProviderName(refresh);
            String providerImage = jwtUtil.getProviderImage(refresh);
            String role = jwtUtil.getRole(refresh);

            // Redis에서 해당 Refresh Token이 유효한지 확인
            String isLogout = (String) redisTemplate.opsForValue().get("RT:" + userId);
            if (ObjectUtils.isEmpty(isLogout)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String newAccessToken = jwtUtil.createJwt("access", userId, provider, providerId, providerName, providerImage, role, ACCESS_EXPIRE);
            String newRefreshToken = jwtUtil.createJwt("refresh", userId, provider, providerId, providerName, providerImage, role, REFRESH_EXPIRE);

            // Redis에 새 Refresh Token 저장
            redisTemplate.opsForValue().set("RT:" + userId, newRefreshToken, REFRESH_EXPIRE, TimeUnit.MILLISECONDS);

            response.addCookie(createCookie("access", newAccessToken));
            response.addCookie(createCookie("refresh", newRefreshToken));
            access = newAccessToken;
        }

        UserOauthDto userOauthDTO = UserOauthDto.builder()
                .userId(jwtUtil.getUserId(access))
                .provider(jwtUtil.getProvider(access))
                .providerId(jwtUtil.getProviderId(access))
                .providerName(jwtUtil.getProviderName(access))
                .profile(jwtUtil.getProviderImage(access))
                .role(jwtUtil.getRole(access))
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOauthDTO);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_EXPIRE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain("ustock.site");
        return cookie;
    }
}
