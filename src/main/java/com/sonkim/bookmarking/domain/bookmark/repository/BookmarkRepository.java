package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // 그룹 내 모든 북마크 페이징 조회
    Page<Bookmark> findAllByTeam_Id(Long teamId, Pageable pageable);

    // 특정 카테고리의 북마크 페이징 조회
    Page<Bookmark> findAllByTeam_IdAndCategory_Id(Long teamId, Long categoryId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Bookmark b SET b.category = null WHERE b.category.id = :categoryId")
    void bulkSetCategoryToNull(@Param("categoryId") Long categoryId);
}
