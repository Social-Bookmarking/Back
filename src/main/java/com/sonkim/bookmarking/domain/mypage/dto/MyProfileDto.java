package com.sonkim.bookmarking.domain.mypage.dto;

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
        private String nickname;
        private String imageKey;
    }
}
