package com.hrth.ustock.jwt;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserOauthDto;
import com.hrth.ustock.exception.domain.user.UserException;
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
import java.util.concurrent.TimeUnit;

import static com.hrth.ustock.exception.domain.user.UserExceptionType.REFRESH_NOT_VALID;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    public static final long ACCESS_EXPIRE = 600000L;
    public static final long REFRESH_EXPIRE = 2592000000L;
    public static final int COOKIE_EXPIRE = 2592000;

    private final String domain;
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
        Cookie refreshCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("access")) {
                access = cookie.getValue();
            }
            if (cookie.getName().equals("refresh")) {
                refreshCookie = cookie;
                refresh = cookie.getValue();
            }
        }

        if ((access == null || access.isEmpty() || jwtUtil.isExpired(access))
                && (refresh == null || refresh.isEmpty() || jwtUtil.isExpired(refresh))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.getCategory(refresh).equals("refresh")) {
            throw new UserException(REFRESH_NOT_VALID);
        }

        Long userId = jwtUtil.getUserId(refresh);
        String redisResult = (String) redisTemplate.opsForValue().get("RT:" + userId);
        if (ObjectUtils.isEmpty(redisResult) || !redisResult.equals(refresh)) {
            throw new ServletException("JWT Refresh 토큰 갱신 필요", new UserException(REFRESH_NOT_VALID));
        }

        String provider = jwtUtil.getProvider(refresh);
        String providerId = jwtUtil.getProviderId(refresh);
        String providerName = jwtUtil.getProviderName(refresh);
        String providerImage = jwtUtil.getProviderImage(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccessToken = jwtUtil.createJwt("access", userId, provider, providerId, providerName, providerImage, role, ACCESS_EXPIRE);
        String newRefreshToken = jwtUtil.createJwt("refresh", userId, provider, providerId, providerName, providerImage, role, REFRESH_EXPIRE);

        redisTemplate.opsForValue().set("RT:" + userId, newRefreshToken, REFRESH_EXPIRE, TimeUnit.MILLISECONDS);

        response.addCookie(createCookie("access", newAccessToken));
        response.addCookie(createCookie("refresh", newRefreshToken));
        access = newAccessToken;

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
        cookie.setDomain(domain);
        return cookie;
    }
}
