package com.sonkim.bookmarking.auth.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String username;
    private String password;
    private String nickname;
}
