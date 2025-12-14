package com.sonkim.bookmarking.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Hidden
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

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("400(BAD_REQUEST) 에러 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        log.warn("409(CONFLICT) 에러 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // HttpRequestMethodNotSupportedException 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("405(METHOD_NOT_ALLOWED) 에러 발생: {}", e.getMessage());

        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "지원되지 않는 HTTP 메서드 요청입니다.");
    }

    // NoResourceFoundException 처리
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("404(NOT_FOUND) 에러 발생 : {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // MethodArgumentNotValidException 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        // 에러 메시지 가공: "필드명: 에러내용, 필드명: 에러내용"
        String errorMessage = bindingResult.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("400(BAD_REQUEST) 유효성 검사 실패: {}", errorMessage);

        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // '좋아요' 중복 에러 처리
    @ExceptionHandler(DuplicateBookmarkLikeException.class)
    public ResponseEntity<?> handleDuplicateBookmarkLikeException(DuplicateBookmarkLikeException e) {
        log.warn("좋아요 중복 시도 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // 그룹 중복 가입 처리
    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<?> handleMemberAlreadyExistsException(MemberAlreadyExistsException e) {
        log.warn("그룹 가입 중복 시도 발생: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // 같은 멱등성 키에 바디 데이터가 다른 경우 예외 처리
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<?> handleUnprocessableEntityException(UnprocessableEntityException e) {
        log.warn("같은 멱등성 키, 다른 바디 데이터 요청 발생ㅣ {}", e.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    // 회원 탈퇴 시 소유주 변경이 필요한 그룹이 있을 경우 예외 처리
    @ExceptionHandler(OwnershipTransferRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleOwnershipTransferRequiredException(OwnershipTransferRequiredException e) {
        log.warn("회원 탈퇴 실패: 소유권 이전 필요. {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("requiredActionGroups", e.getGroups());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 범용 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("예상치 못한 에러 발생", e);

        String errorMsg = "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg);
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

