package com.sonkim.bookmarking.domain.mypage.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String currentPassword;
    private String newPassword;
}
