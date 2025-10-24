package com.sonkim.bookmarking.auth.service;

import com.google.common.net.HttpHeaders;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import com.sonkim.bookmarking.auth.token.dto.TokenDto;
import com.sonkim.bookmarking.auth.dto.RegisterRequestDto;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.repository.UserRepository;
import com.sonkim.bookmarking.domain.profile.entity.Profile;
import com.sonkim.bookmarking.domain.profile.service.ProfileService;
import com.sonkim.bookmarking.auth.token.service.TokenService;
import com.sonkim.bookmarking.common.util.JWTUtil;
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

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final TokenService tokenService;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // 회원 가입
    @Transactional
    public User createAccount(RegisterRequestDto dto) {

        // 아이디 중복 확인
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 닉네임 중복 확인
        if (profileService.nicknameExists(dto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 계정 생성 및 저장
        Profile newProfile = profileService.createProfile(dto.getNickname());
        User newUser = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .profile(newProfile)
                .build();
        userRepository.save(newUser);

        // 기본 개인 그룹 생성
        Team personalTeam = Team.builder()
                .name("개인 공간")
                .owner(newUser)
                .build();
        teamService.saveTeam(personalTeam);

        // 그룹 멤버로 등록 및 ADMIN 권한 부여
        TeamMember membership = TeamMember.builder()
                .user(newUser)
                .team(personalTeam)
                .permission(Permission.ADMIN)
                .build();
        teamMemberService.save(membership);

        // 그룹 초대 코드 생성
        teamService.generateInviteCode(newUser.getId(), personalTeam.getId());

        return newUser;
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

        // Redis에 저장된 토큰과 비교
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsernameFromJWT(refreshToken);
        String storedToken = tokenService.getRefreshToken(userId);

        if (storedToken == null) {
            throw new EntityNotFoundException("서버에 저장된 Refrehs Token이 없거나 만료되었습니다. 다시 로그인해주세요.");
        }

        log.info("userId: {} 토큰 재발급 요청", userId);

        // 일치하지 않는 경우 탈취된 토큰으로 간주하고 DB에서 토큰을 삭제하여 강제 로그아웃 처리
        if (!storedToken.equals(refreshToken)) {
            tokenService.deleteRefreshToken(userId);
            return new ResponseEntity<>("Refresh Token이 일치하지 않습니다. 재발급 필요.", HttpStatus.FORBIDDEN);
        }

        // Access Token과 Refresh Token 재발급(Rotation)
        TokenDto accessTokenDto = jwtUtil.createAccessToken(userId, username);
        TokenDto refreshTokenDto = jwtUtil.createRefreshToken(userId, username);

        // Redis에 저장된 refreshToken 업데이트
        tokenService.saveRefreshToken(userId, refreshTokenDto);

        // 응답 처리
        response.addCookie(createHttpOnlyCookie(refreshTokenDto.getToken()));
        return new ResponseEntity<>(Map.of("accessToken", accessTokenDto.getToken()), HttpStatus.OK);
    }

    // 로그아웃 처리
    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request) {

        // Refresh Token 조회
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        log.info("userId: {} 로그아웃 요청", userId);

        // Redis에서 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

        // 클라이언트 측의 Refresh Token 즉시 만료
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("로그아웃 처리 성공");
    }

    @Transactional
    public ResponseEntity<?> testLogin() {
        User testUser = userRepository.findByUsername("test")
                .orElseGet(() -> createAccount(
                        new RegisterRequestDto("test", "password", "테스트유저"))
                );

        TokenDto accessToken = jwtUtil.createTestToken(testUser.getId(), testUser.getUsername());

        return new ResponseEntity<>(Map.of("accessToken", accessToken.getToken()), HttpStatus.OK);
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
