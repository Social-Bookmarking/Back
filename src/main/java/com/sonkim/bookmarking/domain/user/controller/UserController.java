package com.sonkim.bookmarking.domain.user.controller;

import com.sonkim.bookmarking.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
}
