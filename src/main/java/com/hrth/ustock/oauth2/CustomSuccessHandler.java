package com.hrth.ustock.oauth2;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final long ACCESS_EXPIRE = 600000L;
    public static final long REFRESH_EXPIRE = 86400000L;
    public static final int COOKIE_EXPIRE = 360000;

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        // role, userId, provider, providerId, providerName, providerImage 값 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String access = jwtUtil.createJwt(
                "access",
                customUserDetails.getUserOauthDTO().getUserId(),
                customUserDetails.getUserOauthDTO().getProvider(),
                customUserDetails.getUserOauthDTO().getProviderId(),
                customUserDetails.getName(),
                customUserDetails.getProfile(),
                auth.getAuthority(),
                ACCESS_EXPIRE
        );
        String refresh = jwtUtil.createJwt(
                "refresh",
                customUserDetails.getUserOauthDTO().getUserId(),
                customUserDetails.getUserOauthDTO().getProvider(),
                customUserDetails.getUserOauthDTO().getProviderId(),
                customUserDetails.getName(),
                customUserDetails.getProfile(),
                auth.getAuthority(),
                REFRESH_EXPIRE
        );

        redisTemplate.opsForValue().set(
                "RT:" + customUserDetails.getUserOauthDTO().getUserId(),
                refresh,
                REFRESH_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        response.addCookie(createCookie("access", access));
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
//        response.sendRedirect("https://ustock.site");
        response.sendRedirect("http://localhost:3000/auth/callback");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_EXPIRE);
        cookie.setSecure(true);
        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//        cookie.setDomain(".ustock.site");
        return cookie;
    }
}
