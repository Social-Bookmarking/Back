package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

    // 작성자 기준으로 북마크 조회
    Page<Bookmark> findAllByUser_Id(Long userId, Pageable pageable);

    // 그룹 내 모든 북마크 개수 카운트
    long countByTeam_Id(Long teamId);
}
