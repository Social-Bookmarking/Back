package com.sonkim.bookmarking.auth.controller;

import com.sonkim.bookmarking.auth.service.AuthService;
import com.sonkim.bookmarking.auth.dto.RegisterRequestDto;
import com.sonkim.bookmarking.domain.account.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto dto) {

        log.info("{} 회원가입 요청", dto.toString());

        Account newAccount;

        try {
            newAccount = authService.createAccount(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().body("새로운 계정이 생성되었습니다. accountID = " + newAccount.getId());
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return authService.reissueToken(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}
