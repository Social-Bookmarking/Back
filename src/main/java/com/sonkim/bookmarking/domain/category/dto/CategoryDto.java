package com.sonkim.bookmarking.domain.category.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategoryDto {

    // 카테고리 생성,갱신 요청 DTO
    @Data
    public static class CategoryRequestDto {
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 20, message = "카테고리 이름은 20를 초과할 수 없습니다.")
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
        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Long categoryId;

        @NotNull(message = "순서 값은 필수입니다.")
        @Min(value = 0, message = "순서는 0 이상이어야 합니다.")
        private Integer position;
    }

    @Data
    public static class CategoryOrderUpdateRequest {
        @Valid
        @NotNull(message = "변경할 카테고리 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 카테고리가 있어야 합니다.")
        private List<UpdatePositionRequestDto> categories;
    }
}
