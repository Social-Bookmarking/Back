package com.sonkim.bookmarking.security;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 공개 경로 예외 처리
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 값 추출
        String token = authHeader.substring(7);

        // 카테고리 검증 추가
        String category = jwtUtil.getCategoryFromJWT(token);
        if (!"access".equals(category)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token category");
            return;
        }

        // JWT에서 사용자 정보 추출
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsernameFromJWT(token);

        // 인증 객체 생성
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(userId, username);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetailsImpl, null, userDetailsImpl.getAuthorities());

        // SecurityContext에 인증 정보 저장
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        }
    }
}
