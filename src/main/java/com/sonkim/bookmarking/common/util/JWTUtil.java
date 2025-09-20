package com.sonkim.bookmarking.common.util;

import com.sonkim.bookmarking.auth.token.dto.TokenDto;
import io.jsonwebtoken.Claims;
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
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // application.properties에 저장된 Secret Key를 가져와 설정
    public JWTUtil(@Value("${spring.jwt.secret}") String secret,
                   @Value("${spring.jwt.access-token-expiration-ms}") long accessTokenExpiration,
                   @Value("${spring.jwt.refresh-token-expiration-ms}") long refreshTokenExpiration) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public TokenDto createAccessToken(Long userId, String username) {
        return createToken("access", userId, username, accessTokenExpiration);
    }

    // Refresh Token 생성
    public TokenDto createRefreshToken(Long userId, String username) {
        return createToken("refresh", userId, username, refreshTokenExpiration);
    }

    // 공통 토큰 생성 로직
    private TokenDto createToken(String category, Long userId, String username, long expirationMs) {
        long now = System.currentTimeMillis();
        long expiresAt = now + expirationMs;
        String token = Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(new Date(now))
                .expiration(new Date(expiresAt))
                .signWith(secretKey)
                .compact();

        return TokenDto.builder()
                .token(token)
                .expiresAt(expirationMs)
                .build();
    }

    // 테스트용 Access Tocket 메서드 생성
    public TokenDto createTestToken(Long userId, String username) {
        long testTokenExpiration = 86400000;
        return createToken("access", userId, username, testTokenExpiration);
    }

    // JWT 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return getPayload(token).getExpiration().before(new Date());
    }

    // JWT에서 userId 추출
    public Long getUserId(String token) {
        return getPayload(token).get("userId", Long.class);
    }

    // JWT에서 username 추출
    public String getUsernameFromJWT(String token) {
        return getPayload(token).get("username", String.class);
    }

    // JWT 카테고리 확인
    public String getCategoryFromJWT(String token) {
        return getPayload(token).get("category", String.class);
    }

    // 공통 토큰 페이로드 추출 로직
    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
