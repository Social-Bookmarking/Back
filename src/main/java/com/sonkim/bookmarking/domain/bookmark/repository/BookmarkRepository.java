package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> getBookmarksByTeam_Id(Long teamId);

    List<Bookmark> getBookmarksByTeam_IdAndCategory_Id(Long teamId, Long categoryId);
}
