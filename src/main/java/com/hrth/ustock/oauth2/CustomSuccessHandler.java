package com.hrth.ustock.oauth2;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest req, HttpServletResponse res, Authentication authentication)
            throws IOException, ServletException {

        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        // jwt를 만들 때 role, userId, provider, providerId 값으로 jwt를 만들었으니 그 값을 가져오기
        Long userId = customUserDetails.getUserId();
        String provider = customUserDetails.getProvider();
        String providerId = customUserDetails.getProviderId();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 600000L = token expire time (10분)
        String access = jwtUtil.createJwt("access", userId, provider, providerId, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", userId, provider, providerId, role, 86400000L);

        // redis에 refresh 저장
        redisTemplate.opsForValue().set("RT:" + userId, refresh, 86400000L, TimeUnit.MILLISECONDS);

        // 응답 설정
        res.addCookie(createCookie("access", access));
        res.addCookie(createCookie("refresh", refresh));
        res.setStatus(HttpStatus.OK.value());
        res.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(100 * 60 * 60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        // 자바스크립트가 쿠키를 가져가지 못하도록 HTTP only
        cookie.setHttpOnly(true);

        return cookie;
    }
}
