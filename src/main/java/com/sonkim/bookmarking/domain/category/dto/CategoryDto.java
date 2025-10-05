package com.sonkim.bookmarking.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryDto {

    // 카테고리 생성,갱신 요청 DTO
    @Data
    public static class CategoryRequestDto {
        private String name;
    }

    // 카테고리 목록 조회 DTO
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryResponseDto {
        private Long id;
        private String name;
        private Long bookmarkCount;
    }

    // 카테고리 순서 업데이트 요청 DTO
    @Data
    public static class UpdatePositionRequestDto {
        private Long categoryId;
        private Integer position;
    }
}
