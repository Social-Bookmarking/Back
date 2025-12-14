package com.sonkim.bookmarking.domain.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class MyProfileDto {

    @Data
    @Builder
    public static class MyProfileResponseDto {
        private String nickname;
        private String profileImageUrl;
    }

    @Data
    public static class UpdateRequestDto {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 20자를 초과할 수 없습니다.")
        private String nickname;

        private String imageKey;
    }
}
