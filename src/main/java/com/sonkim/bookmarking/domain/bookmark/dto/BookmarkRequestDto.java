package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookmarkRequestDto {
    private Long categoryId;
    private String url;
    private String title;
    private String description;
    private List<String> tagNames;
    private Double latitude;
    private Double longitude;
    private String imageKey;            // S3에 직접 업로드한 경우의 파일 키
    private String originalImageUrl;    // OG 정보의 이미지 URL
}
