package com.sonkim.bookmarking.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class TagDto {

    // 태그 수정 요청 DTO
    @Data
    public static class TagRequestDto {
        @NotBlank(message = "태그 이름은 필수입니다.")
        @Size(max = 20, message = "태그 이름은 20자를 초과할 수 없습니다.")
        private String name;
    }

    // 그룹 내 모든 태그 응답 DTO
    @Data
    @Builder
    public static class TagResponseDto {
        private Long id;
        private String name;
    }
}
