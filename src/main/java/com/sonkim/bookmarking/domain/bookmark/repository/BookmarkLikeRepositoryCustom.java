package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.dto.LikeCountDto;

import java.util.List;

public interface BookmarkLikeRepositoryCustom {
    // 여러 북마크의 좋아요 개수 한 번에 조회
    List<LikeCountDto> findLikesCountForBookmarks(List<Long> bookmarkIds);

    // 특정 사용자가 좋아요를 누른 북마크 조회
    List<Long> findLikedBookmarkIdsForUser(Long userId, List<Long> bookmarkIds);
}
