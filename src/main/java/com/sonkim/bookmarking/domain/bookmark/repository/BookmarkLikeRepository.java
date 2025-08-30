package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.dto.LikeCountDto;
import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkLikeRepository extends JpaRepository<BookmarkLike, Long> {
    boolean existsBookmarkLikeByUser_IdAndBookmark_Id(Long userId, Long bookmarkId);

    long countBookmarkLikesByBookmark_Id(Long bookmarkId);

    // 여러 북마크의 좋아요 개수 한 번에 조회
    @Query("SELECT new com.sonkim.bookmarking.domain.bookmark.dto.LikeCountDto(bl.bookmark.id, COUNT(bl.id)) " +
            "FROM BookmarkLike bl WHERE bl.bookmark.id IN :bookmarkIds GROUP BY bl.bookmark.id")
    List<LikeCountDto> findLikesCountForBookmarks(@Param("bookmarkIds") List<Long> bookmarksIds);

    // 특정 사용자가 좋아요를 누른 북마크 조회
    @Query("SELECT bl.bookmark.id FROM BookmarkLike bl " +
            "WHERE bl.user.id = :userId AND bl.bookmark.id IN :bookmarkIds")
    List<Long> findLikedBookmarkIdsForUser(@Param("userId") Long userId, @Param("bookmarkIds") List<Long> bookmarkIds);

    Optional<BookmarkLike> findByUser_IdAndBookmark_Id(Long userId, Long bookmarkId);
}
