package com.sonkim.bookmarking.auth.dto;

import lombok.Builder;
import lombok.Data;

public class AuthDto {

    @Data
    @Builder
    public static class TokenResponseDto {
        private String token;
    }
}
