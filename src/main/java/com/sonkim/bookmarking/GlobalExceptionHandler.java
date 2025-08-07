package com.sonkim.bookmarking;

import com.sonkim.bookmarking.exception.DuplicateBookmarkLikeException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
// @Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    // AuthorizationDeniedException 처리
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("403(FORBIDDEN) 에러 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // EntityNotFoundException 처리
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("404(NOT_FOUND) 에러 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // '좋아요' 중복 에러 처리
    @ExceptionHandler(DuplicateBookmarkLikeException.class)
    public ResponseEntity<?> handleDuplicateBookmarkLikeException(DuplicateBookmarkLikeException e) {
        log.warn("좋아요 중복 시도 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);

        return new ResponseEntity<>(error, status);
    }
}

