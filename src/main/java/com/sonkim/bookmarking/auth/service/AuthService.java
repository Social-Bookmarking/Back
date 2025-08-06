package com.sonkim.bookmarking.auth.service;

import com.google.common.net.HttpHeaders;
import com.sonkim.bookmarking.domain.token.dto.TokenDto;
import com.sonkim.bookmarking.domain.token.entity.Token;
import com.sonkim.bookmarking.auth.dto.RegisterRequestDto;
import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.repository.AccountRepository;
import com.sonkim.bookmarking.domain.profile.entity.Profile;
import com.sonkim.bookmarking.domain.profile.service.ProfileService;
import com.sonkim.bookmarking.domain.token.service.TokenService;
import com.sonkim.bookmarking.util.CryptoUtil;
import com.sonkim.bookmarking.util.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final ProfileService profileService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // 회원 가입
    @Transactional
    public Account createAccount(RegisterRequestDto dto) {

        log.info("{} 회원가입 요청", dto.toString());

        // 아이디 중복 확인
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 닉네임 중복 확인
        if (profileService.nicknameExists(dto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Profile newProfile = profileService.createProfile(dto.getNickname());

        Account account = Account.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .profile(newProfile)
                .build();

        return accountRepository.save(account);
    }

    // 토큰 재발급
    @Transactional
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {

        // 요청 쿠키에서 Refresh Token 추출
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        // JWT 유효성 검증
        if(jwtUtil.isTokenExpired(refreshToken)) {
            return new ResponseEntity<>("만료된 Refresh Token입니다.", HttpStatus.UNAUTHORIZED);
        }

        if (!"refresh".equals(jwtUtil.getCategoryFromJWT(refreshToken))) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        // DB에 저장된 토큰과 비교
        Long accountId = jwtUtil.getAccountIdFromJWT(refreshToken);
        String username = jwtUtil.getUsernameFromJWT(refreshToken);
        Token storedToken = tokenService.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("DB에서 Refresh Token을 찾을 수 없습니다."));

        log.info("accountId: {} 토큰 재발급 요청", accountId);

        // 일치하지 않는 경우 탈취된 토큰으로 간주하고 DB에서 토큰을 삭제하여 강제 로그아웃 처리
        if (!storedToken.getRefreshToken().equals(CryptoUtil.hash(refreshToken))) {
            tokenService.deleteTokenByRefreshToken(storedToken.getRefreshToken());
            return new ResponseEntity<>("DB와 토큰이 일치하지 않습니다. 재발급 필요.", HttpStatus.FORBIDDEN);
        }

        // Access Token과 Refresh Token 재발급(Rotation)
        TokenDto accessTokenDto = jwtUtil.createAccessToken(accountId, username);
        TokenDto refreshTokenDto = jwtUtil.createRefreshToken(accountId, username);

        // DB에 저장된 refreshToken 업데이트
        storedToken.updateToken(CryptoUtil.hash(refreshTokenDto.getToken()), refreshTokenDto.getExpiresAt());

        // 응답 처리
        response.addCookie(createHttpOnlyCookie(refreshTokenDto.getToken()));
        return new ResponseEntity<>(Map.of("accessToken", accessTokenDto.getToken()), HttpStatus.OK);
    }

    // 로그아웃 처리
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        // Refresh Token 조회
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        // DB에서 Refresh Token 삭제
        tokenService.deleteTokenByRefreshToken(refreshToken);

        // 클라이언트 측의 Refresh Token 즉시 만료
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("로그아웃 처리 성공");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    // HttpOnlyCookie 생성 메서드
    private Cookie createHttpOnlyCookie(String value) {
        Cookie cookie = new Cookie("refreshToken", value);
        cookie.setMaxAge(7*24*60*60); // 7일
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
