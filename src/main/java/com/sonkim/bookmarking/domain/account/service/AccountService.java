package com.sonkim.bookmarking.domain.account.service;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.repository.AccountRepository;
import com.sonkim.bookmarking.domain.profile.service.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ProfileService profileService;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));
    }


}
