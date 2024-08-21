package com.hrth.ustock.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getUserId(String token) {
        return getPayload(token).get("userId", Long.class);
    }

    public String getProvider(String token) {
        return getPayload(token).get("provider", String.class);
    }

    public String getProviderId(String token) {
        return getPayload(token).get("providerId", String.class);
    }

    public String getProviderName(String token) { return getPayload(token).get("providerName", String.class); }

    public String getProviderImage(String token) { return getPayload(token).get("providerImage", String.class); }

    public String getCategory(String token) {
        return getPayload(token).get("category", String.class);
    }

    public String getRole(String token) {
        return getPayload(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try{
            return getPayload(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String createJwt(String category, Long userId, String provider, String providerId, String providerName, String providerImage, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("provider", provider)
                .claim("providerId", providerId)
                .claim("providerName", providerName)
                .claim("providerImage", providerImage)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
