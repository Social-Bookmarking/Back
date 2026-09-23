package com.sonkim.bookmarking.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OpenGraphImageCache {
    static final Duration IMAGE_TTL = Duration.ofHours(12);
    static final Duration REFRESH_LOCK_TTL = Duration.ofMinutes(1);
    private static final String IMAGE_KEY_PREFIX = "og:image:v1:";
    private static final String LOCK_KEY_PREFIX = "og:image:refresh-lock:v1:";
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public OpenGraphImageCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String get(String pageUrl) {
        try {
            return redisTemplate.opsForValue().get(imageKey(pageUrl));
        } catch (RuntimeException e) {
            log.warn("OpenGraph 이미지 캐시 조회 실패. url={}", pageUrl, e);
            return null;
        }
    }

    public void put(String pageUrl, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(imageKey(pageUrl), imageUrl, IMAGE_TTL);
        } catch (RuntimeException e) {
            log.warn("OpenGraph 이미지 캐시 저장 실패. url={}", pageUrl, e);
        }
    }

    public String tryAcquireRefreshLock(String pageUrl) {
        String token = UUID.randomUUID().toString();
        try {
            boolean acquired = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(lockKey(pageUrl), token, REFRESH_LOCK_TTL));
            return acquired ? token : null;
        } catch (RuntimeException e) {
            log.warn("OpenGraph 이미지 갱신 잠금 획득 실패. url={}", pageUrl, e);
            return null;
        }
    }

    public void releaseRefreshLock(String pageUrl, String token) {
        try {
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(lockKey(pageUrl)), token);
        } catch (RuntimeException e) {
            log.warn("OpenGraph 이미지 갱신 잠금 해제 실패. url={}", pageUrl, e);
        }
    }

    static String imageKey(String pageUrl) {
        return IMAGE_KEY_PREFIX + hash(normalizePageUrl(pageUrl));
    }

    static String normalizePageUrl(String pageUrl) {
        if (pageUrl == null || pageUrl.isBlank()) {
            throw new IllegalArgumentException("북마크 URL이 필요합니다.");
        }

        URI uri = URI.create(pageUrl.trim());
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null) {
            throw new IllegalArgumentException("유효한 북마크 URL이 아닙니다.");
        }

        scheme = scheme.toLowerCase();
        host = host.toLowerCase();
        int port = uri.getPort();
        if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
            port = -1;
        }

        StringBuilder normalized = new StringBuilder(scheme).append("://");
        if (uri.getRawUserInfo() != null) {
            normalized.append(uri.getRawUserInfo()).append('@');
        }
        if (host.contains(":")) {
            normalized.append('[').append(host).append(']');
        } else {
            normalized.append(host);
        }
        if (port != -1) {
            normalized.append(':').append(port);
        }
        if (uri.getRawPath() != null) {
            normalized.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            normalized.append('?').append(uri.getRawQuery());
        }
        return normalized.toString();
    }

    private static String lockKey(String pageUrl) {
        return LOCK_KEY_PREFIX + hash(normalizePageUrl(pageUrl));
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
