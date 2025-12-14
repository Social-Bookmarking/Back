package com.sonkim.bookmarking.domain.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordDto {
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호는 필수입니다.")
    private String newPassword;
}
