package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkSearchCond;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepositoryCustom {
    // 검색 조건을 받아와 검색 처리
    Page<Bookmark> search(Long teamId, BookmarkSearchCond cond, Pageable pageable);

    // 북마크 조회 시 북마크-태그와 태그 정보 모두 가져오는 쿼리
    Optional<Bookmark> findByIdWithTags(@Param("id") Long id);

    // 카테고리 삭제 시 연관되어 있는 북마크들 카테고리 null로 변경 (벌크 연산)
    void bulkSetCategoryToNull(@Param("categoryId") Long categoryId);

    // 특정 사용자가 '좋아요'를 누른 북마크 정보 조회
    Page<Bookmark> findLikedBookmarksByUser_Id(Long userId, Pageable pageable);
}
