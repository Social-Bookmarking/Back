package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;

@Data
public class LikeCountDto {
    Long bookmarkId;
    Long count;

    LikeCountDto(Long bookmarkId, Long count) {
        this.bookmarkId = bookmarkId;
        this.count = count;
    }
}
