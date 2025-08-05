package com.sonkim.bookmarking.domain.account.controller;

import com.sonkim.bookmarking.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
}
