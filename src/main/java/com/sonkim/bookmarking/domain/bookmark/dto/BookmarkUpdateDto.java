package com.sonkim.bookmarking.domain.bookmark.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BookmarkUpdateDto {
    private Long categoryId;
    private String url;

    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 5000)
    private String description;

    @Size(max = 10, message = "태그는 최대 10개까지만 등록할 수 있습니다.")
    private List<String> tagNames;

    @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
    @Max(value = 90, message = "위도는 90 이하여야 합니다.")
    private Double latitude;

    @Min(value = -180, message = "경도는 -180 이상이어야 합니다.")
    @Max(value = 180, message = "경도는 180 이하여야 합니다.")
    private Double longitude;
    private String imageKey;            // S3에 직접 업로드한 경우의 파일 키
    private String originalImageUrl;    // OG 정보의 이미지 URL
}
