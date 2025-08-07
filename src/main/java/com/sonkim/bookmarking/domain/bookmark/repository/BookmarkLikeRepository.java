package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkLikeRepository extends JpaRepository<BookmarkLike, Long> {
    boolean existsBookmarkLikeByAccount_IdAndBookmark_Id(Long accountId, Long bookmarkId);

    long countBookmarkLikesByBookmark_Id(Long bookmarkId);

    Optional<BookmarkLike> findByAccount_IdAndBookmark_Id(Long accountId, Long bookmarkId);
}
