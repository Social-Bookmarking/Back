package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;

@Data
public class LikeCountDto {
    private Long bookmarkId;
    private Long count;

    public LikeCountDto(Long bookmarkId, Long count) {
        this.bookmarkId = bookmarkId;
        this.count = count;
    }
}
