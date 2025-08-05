package com.sonkim.bookmarking.domain.token.service;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.service.AccountService;
import com.sonkim.bookmarking.domain.token.dto.TokenDto;
import com.sonkim.bookmarking.domain.token.entity.Token;
import com.sonkim.bookmarking.domain.token.repository.TokenRepository;
import com.sonkim.bookmarking.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final AccountService accountService;

    @Transactional
    public void deleteTokenByAccountId(Long accountId) {
        tokenRepository.deleteTokenByAccountId(accountId);
    }

    @Transactional
    public void saveToken(long accountId, TokenDto dto) {

        // accountId로 Account 조회
        Account account = accountService.getAccountById(accountId);

        // 발급된 refreshToken 저장
        Token newToken = Token.builder()
                .account(account)
                .refreshToken(CryptoUtil.hash(dto.getToken()))
                .expiresAt(dto.getExpiresAt())
                .build();

        tokenRepository.save(newToken);
    }

}
