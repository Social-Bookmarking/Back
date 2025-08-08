package com.sonkim.bookmarking.domain.token.service;

import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.service.UserService;
import com.sonkim.bookmarking.domain.token.dto.TokenDto;
import com.sonkim.bookmarking.domain.token.entity.Token;
import com.sonkim.bookmarking.domain.token.repository.TokenRepository;
import com.sonkim.bookmarking.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final UserService userService;

    @Transactional
    public void saveToken(long userId, TokenDto dto) {

        // userId로 Account 조회
        User user = userService.getUserById(userId);

        // 발급된 refreshToken 저장
        Token newToken = Token.builder()
                .user(user)
                .refreshToken(CryptoUtil.hash(dto.getToken()))
                .expiresAt(dto.getExpiresAt())
                .build();

        tokenRepository.save(newToken);
    }

    @Transactional(readOnly = true)
    public Optional<Token> findByUserId(long userId) {
        return tokenRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteTokenByRefreshToken(String refreshToken) {
        tokenRepository.deleteByRefreshToken(CryptoUtil.hash(refreshToken));
    }

    @Transactional
    public void deleteTokenByUserId(Long userId) {
        tokenRepository.deleteTokenByUserId(userId);
    }
}
