package com.hrth.ustock.jwt;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserOauthDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        // null check
        if(cookies == null || cookies.length == 0) {
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

            // 토큰이 access인지 확인 (발급시 페이로드에 명시)
            String category = jwtUtil.getCategory(access);

            // 토큰이 access가 아닌 경우 UNAUTHORIZED return
            // 해결방법은 access token Application에서 수동 삭제
            if (!category.equals("access")) {
                PrintWriter writer = response.getWriter();
                writer.print("invalid access token");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // Access Token이 만료된 경우 Refresh Token으로 재발급
            if (refresh != null && !jwtUtil.isExpired(refresh)
                    && jwtUtil.getCategory(refresh).equals("refresh")) {
                Long userId = jwtUtil.getUserId(refresh);
                String provider = jwtUtil.getProvider(refresh);
                String providerId = jwtUtil.getProviderId(refresh);
                String role = jwtUtil.getRole(refresh);

                // Redis에서 해당 Refresh Token이 유효한지 확인
                String isLogout = (String) redisTemplate.opsForValue().get("RT:" + userId);
                if (ObjectUtils.isEmpty(isLogout)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 해당 코드와 CustomSuccessHandler가 겹침 - 추후에 수정
                // 새로운 Access Token 생성
                String newAccessToken = jwtUtil.createJwt("access", userId, provider, providerId, role, 600000L);
                String newRefreshToken = jwtUtil.createJwt("refresh", userId, provider, providerId, role, 86400000L);

                // Redis에 새 Refresh Token 저장
                redisTemplate.opsForValue().set("RT:" + userId, newRefreshToken, 86400000L, TimeUnit.MILLISECONDS);

                // 클라이언트에 새로운 Access Token과 Refresh Token 전송
                response.addCookie(createCookie("access", newAccessToken));
                response.addCookie(createCookie("refresh", newRefreshToken));

                // 새 Access Token으로 사용자 정보 설정
                access = newAccessToken;
            } else {
                // Refresh Token이 유효하지 않은 경우
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // 토큰에서 username과 role 획득 후 userOauthDTO에 값 set
        UserOauthDTO userOauthDTO = UserOauthDTO.builder()
                .userId(jwtUtil.getUserId(access))
                .provider(jwtUtil.getProvider(access))
                .providerId(jwtUtil.getProviderId(access))
                .role(jwtUtil.getRole(access))
                .build();

        // UserDetails에 회원 객체 정보 담기
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
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
