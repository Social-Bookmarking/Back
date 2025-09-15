package com.sonkim.bookmarking.domain.token.service;

import com.sonkim.bookmarking.domain.token.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Refresh Token을 Redis에 저장
    public void saveRefreshToken(long userId, TokenDto refreshTokenDto) {
        redisTemplate.opsForValue().set(
                "RT:" + userId,
                refreshTokenDto.getToken(),
                Duration.ofMillis(refreshTokenDto.getExpiresAt())
        );
    }

    // Redis에서 Refresh Token 조회
    public String getRefreshToken(Long userId) {
        return (String) redisTemplate.opsForValue().get("RT:" + userId);
    }

    // Redis에서 Refresh Token 수동 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("RT:" + userId);
    }
}
