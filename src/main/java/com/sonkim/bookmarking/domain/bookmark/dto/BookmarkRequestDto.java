package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookmarkRequestDto {
    private Long categoryId;
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private List<Long> tagIds;
    private Double latitude;
    private Double longitude;
}
