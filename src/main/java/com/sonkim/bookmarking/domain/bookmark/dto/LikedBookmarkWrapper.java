package com.sonkim.bookmarking.domain.bookmark.dto;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikedBookmarkWrapper {
    private Bookmark bookmark;
    private Long bookmarkLikeId;
}
