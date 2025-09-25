package com.sonkim.bookmarking.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonkim.bookmarking.common.exception.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCheckAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Idempotent 어노테이션이 붙은 메서드를 대상으로 실행
    @Around("@annotation(com.sonkim.bookmarking.common.aop.Idempotent)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 헤더에서 멱등키 조회
        String idempotencyKey = getIdempotencyKey(request);

        // 요청에서 바디 조회 및 Hash 값으로 변환
        byte[] requestData = getRequestData(request);
        String requestHash = createHash(requestData);

        // Redis 조회
        String storedValueJson = (String) redisTemplate.opsForValue().get(idempotencyKey);

        // 전달된 멱등성 키에 대해 저장된 내용이 존재
        if (storedValueJson != null) {
            // 아직 처리 중이면
            if (storedValueJson.equals("in_progress")) {
                log.warn("멱등성 키 중복 (처리 중): {}", idempotencyKey);
                return new ResponseEntity<>("처리 중인 요청입니다.", HttpStatus.CONFLICT);
            } else {
                // 처리가 완료되었으면
                IdempotencyData storedData = objectMapper.readValue(storedValueJson, IdempotencyData.class);

                    // 동일한 멱등성 키, 다른 요청 내용
                    if (!requestHash.equals(storedData.getRequestHash())) {
                        log.error("멱등성 키 재사용 오류: 키는 같지만 요청한 내용이 다릅니다. Key: {}", idempotencyKey);
                        throw new UnprocessableEntityException("동일한 Idempotency-Key에 다른 요청을 보낼 수 없습니다.");
                    }

                    // 완전히 동일한 요청. 중복 요청 처리
                    log.info("멱등성 키 중복 (처리 완료): {}. 저장된 응답 반환", idempotencyKey);
                    return ResponseEntity
                            .status(storedData.getStatusCode())
                            .contentType(MediaType.APPLICATION_JSON) // 컨텐츠 타입 지정
                            .body(storedData.getResponseBody());
            }
        }

        // 새로운 멱등성 키에 대한 처리
        try {
            redisTemplate.opsForValue().set(idempotencyKey, "in_progress", 1, TimeUnit.MINUTES);
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) joinPoint.proceed();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                IdempotencyData dataToStore = new IdempotencyData(
                        requestHash,
                        responseEntity.getStatusCode().value(),
                        objectMapper.writeValueAsString(responseEntity.getBody())
                );
                String dataToStoreJson = objectMapper.writeValueAsString(dataToStore);
                redisTemplate.opsForValue().set(idempotencyKey, dataToStoreJson, 5, TimeUnit.MINUTES);
            }

            return responseEntity;
        } catch (Exception e) {
            // 메서드 실행 중 예외 발생 시 'in_progress' 상태를 삭제하여 재시도 허용
            redisTemplate.delete(idempotencyKey);
            throw e;
        }

    }

    private String getIdempotencyKey(HttpServletRequest request) {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        // 키가 없으면 예외 발생
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key 헤더가 누락되었습니다.");
        }

        return idempotencyKey;
    }

    private byte[] getRequestData(HttpServletRequest request) {
        // 🔽 ContentCachingRequestWrapper를 직접 찾아내는 로직으로 수정
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

        if (requestWrapper != null) {
            return requestWrapper.getContentAsByteArray();
        }

        // POST 요청이 아니거나 Body가 없는 경우
        return new byte[0];
    }

    private String createHash(byte[] requestData) {
        if (requestData == null || requestData.length == 0) {
            return "";
        }
        return DigestUtils.md5DigestAsHex(requestData);
    }

    @Getter
    @AllArgsConstructor
    private static class IdempotencyData implements Serializable {
        private String requestHash;
        private int statusCode;
        private String responseBody;
    }
}
