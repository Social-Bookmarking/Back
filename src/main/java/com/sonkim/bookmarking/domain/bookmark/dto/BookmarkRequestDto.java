package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;

@Data
public class BookmarkRequestDto {
    private Long categoryId;
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
}
