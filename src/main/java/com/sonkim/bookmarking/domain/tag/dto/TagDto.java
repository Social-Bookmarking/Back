package com.sonkim.bookmarking.domain.tag.dto;

import lombok.Builder;
import lombok.Data;

public class TagDto {

    // 태그 생성/수정 요청 DTO
    @Data
    public static class RequestDto {
        private String name;
    }

    // 그룹 내 모든 태그 응답 DTO
    @Data
    @Builder
    public static class ResponseDto {
        private Long id;
        private String name;
    }
}
