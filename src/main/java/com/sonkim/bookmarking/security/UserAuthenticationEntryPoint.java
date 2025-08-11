package com.sonkim.bookmarking.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        log.info("AuthenticationEntryPoint가 실행된 경로: {}", request.getRequestURI());

        // Swagger 문서 호출 중 발생하는 예외는 로그 기록 X
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
            return;
        }

        // 인증 과정에서 예외 발생 시 처리
        log.error("인증되지 않은 사용자의 접근: {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다.");
    }
}
