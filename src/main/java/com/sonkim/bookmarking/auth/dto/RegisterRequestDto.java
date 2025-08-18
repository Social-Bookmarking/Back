package com.sonkim.bookmarking.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequestDto {
    private String username;
    private String password;
    private String nickname;
}
