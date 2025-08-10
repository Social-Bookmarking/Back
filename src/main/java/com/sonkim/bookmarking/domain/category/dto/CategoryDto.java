package com.sonkim.bookmarking.domain.category.dto;

import lombok.Builder;
import lombok.Data;

public class CategoryDto {

    // 카테고리 생성,갱신 요청 DTO
    @Data
    public static class RequestDto {
        private String name;
        private String description;
    }

    // 카테고리 목록 조회 DTO
    @Data
    @Builder
    public static class ResponseDto {
        private Long id;
        private String name;
        private String description;
    }
}
