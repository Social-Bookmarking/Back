package com.sonkim.bookmarking.domain.mypage.dto;

import lombok.Builder;
import lombok.Data;

public class MyProfileDto {

    @Data
    @Builder
    public static class MyProfileRequestDto {
        private String nickname;
        private String imageUrl;
    }

    @Data
    public static class UpdateNicknameRequestDto {
        private String nickname;
    }
}
