package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> getBookmarksByTeam_Id(Long teamId);

    List<Bookmark> getBookmarksByTeam_IdAndCategory_Id(Long teamId, Long categoryId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Bookmark b SET b.category = null WHERE b.category.id = :categoryId")
    void bulkSetCategoryToNull(@Param("categoryId") Long categoryId);
}
