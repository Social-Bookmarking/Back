package com.sonkim.bookmarking.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonkim.bookmarking.auth.dto.LoginRequestDto;
import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.token.dto.TokenDto;
import com.sonkim.bookmarking.domain.token.service.TokenService;
import com.sonkim.bookmarking.common.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        // 로그인 요청 URL
        setFilterProcessesUrl("/api/auth/login");
    }

    // 로그인 요청 시 사용자 인증 처리
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청 본문을 JSON으로 반환
            LoginRequestDto loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            // username과 비밀번호 추출
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // 인증용 토큰 생성
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // 인증 진행
            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException("로그인 데이터 파싱 중 오류 발생.", e);
        }
    }

    // 인증 성공 시 JWT 토큰 발급
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

        // 인증된 사용자 정보 가져오기
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        Long userId = userDetails.getId();
        String username = userDetails.getUsername();

        // Access Token, Refresh Token 생성
        TokenDto accessToken = jwtUtil.createAccessToken(userId, username);
        TokenDto refreshToken = jwtUtil.createRefreshToken(userId, username);

        // 중복 로그인을 방지하기 위해 기존 토큰 삭제
        tokenService.deleteRefreshToken(userId);

        // 새로운 Refresh Token 저장
        tokenService.saveRefreshToken(userId, refreshToken);

        // Refresh Token은 HttpOnly 쿠키에 저장
        response.addCookie(createHttpOnlyCookie(refreshToken.getToken()));

        // Access Token은 JSON 형태로 응답 본문에 포함하여 전달
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        PrintWriter out = response.getWriter();
        out.print("{\"accessToken\": \"" + accessToken.getToken() + "\"}");
        out.flush();
    }

    // HttpOnlyCookie 생성 메서드
    private Cookie createHttpOnlyCookie(String value) {
        Cookie cookie = new Cookie("refreshToken", value);
        cookie.setMaxAge(7*24*60*60); // 7일
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    // 인증 실패 시 오류 메시지 전송
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {

        // HTTP 상태 코드 401 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 에러 메시지 설정
        String errorMessage = "{\"error\": \"아이디 또는 비밀번호가 일치하지 않습니다.\"}";
        PrintWriter out = response.getWriter();
        out.print(errorMessage);
        out.flush();
    }
}