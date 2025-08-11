package com.sonkim.bookmarking.auth.controller;

import com.sonkim.bookmarking.auth.service.AuthService;
import com.sonkim.bookmarking.auth.dto.RegisterRequestDto;
import com.sonkim.bookmarking.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Tag(name = "인증 관리", description = "회원가입, 로그인, 토큰 재발급, 로그아웃 관련 API")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 등록 시 개인용 기본 그룹이 함께 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이메일 형식 오류, 비밀번호 길이 부족)"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto dto) {

        log.info("{} 회원가입 요청", dto.toString());
        User newUser = authService.createAccount(dto);

        Map<String, Object> response = Map.of(
                "message", "새로운 계정이 생성되었습니다.",
                "userId", newUser.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token을 이용해 재발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공 (Authorization 헤더에 새로운 Access Token 포함)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return authService.reissueToken(request, response);
    }

    @Operation(summary = "로그아웃", description = "서버에 저장된 Refresh Token을 삭제하여 현재 세션을 무효화합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}
