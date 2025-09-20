package com.sonkim.bookmarking.common.aop;

import com.sonkim.bookmarking.common.exception.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCheckAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    // Idempotent 어노테이션이 붙은 메서드를 대상으로 실행
    @Around("@annotation(com.sonkim.bookmarking.common.aop.Idempotent)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 헤더에서 멱등키 조회
        String idempotencyKey = getIdempotencyKey(request);

        // 바디 조회
        String requestBody = getRequestBody(request);

        log.info("[IdempotentAspect] ({}) 요청 데이터 :: {}", idempotencyKey, requestBody);

        // 중복 요청 확인
        Boolean isPoss = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, requestBody, 5, TimeUnit.MINUTES);

        // 중복되는 요청인 경우 예외처리
        if (Boolean.FALSE.equals(isPoss)) {
            handleRequestException(idempotencyKey, requestBody);
        }

        // 키가 없으면 Redis에 저장
        redisTemplate.opsForValue().set(idempotencyKey, requestBody, 5, TimeUnit.MINUTES);

        // 원래 메서드 실행
        return joinPoint.proceed();
    }

    private String getIdempotencyKey(HttpServletRequest request) {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        // 키가 없으면 예외 발생
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key가 없습니다.");
        }

        return idempotencyKey;
    }

    private String getRequestBody(HttpServletRequest request) {
        // 🔽 ContentCachingRequestWrapper를 직접 찾아내는 로직으로 수정
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

        if (requestWrapper != null) {
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, StandardCharsets.UTF_8);
            }
        }

        // POST 요청이 아니거나 Body가 없는 경우
        return "";
    }

    private void handleRequestException(String idempotencyKey, String requestBody) {
        // 기존 데이터 조회
        String originRequestBody = (String) redisTemplate.opsForValue().get(idempotencyKey);
        log.info("[IdempotentAspect] ({}) 기존 요청 데이터 :: {}", idempotencyKey, originRequestBody);

        // 기존 요청 데이터와 일치하지 않으면 잘못된 요청
        if(!idempotencyKey.isBlank() && !requestBody.equals(originRequestBody))
            throw new UnprocessableEntityException("다른 요청이지만 같은 멱등키가 사용되었습니다.");
        else
            throw new IllegalStateException("이전 요청이 아직 처리되지 않았습니다.");
    }
}
