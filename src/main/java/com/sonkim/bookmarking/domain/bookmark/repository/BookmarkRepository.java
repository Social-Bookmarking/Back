package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // 그룹 내 모든 북마크 페이징 조회
    Page<Bookmark> findAllByTeam_Id(Long teamId, Pageable pageable);

    // 특정 카테고리의 북마크 페이징 조회
    Page<Bookmark> findAllByTeam_IdAndCategory_Id(Long teamId, Long categoryId, Pageable pageable);

    // 검색 키워드를 바탕으로 북마크 페이징 조회
    @Query("SELECT b FROM Bookmark b " +
            "WHERE b.team.id = :teamId " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Bookmark> findAllByTeam_IdAndKeyword(@Param("teamId") Long teamId,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    // 검색 키워드, 카테고리 바탕으로 북마크 페이징 조회
    @Query("SELECT b FROM Bookmark b " +
            "WHERE b.team.id = :teamId AND b.category.id = :categoryId " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
     Page<Bookmark> findAllByTeam_IdAndCategory_IdAndKeyword(@Param("teamId") Long teamId,
                                                             @Param("categoryId") Long categoryId,
                                                             @Param("keyword") String keyword,
                                                             Pageable pageable);

    // 그룹 내에서 특정 태그를 가진 북마크 조회
    @Query("SELECT b FROM Bookmark b JOIN b.bookmarkTags bt WHERE b.team.id = :teamId AND bt.tag.id = :tagId")
    Page<Bookmark> findByTeam_IdAndTag_Id(@Param("teamId") Long teamId,
                                          @Param("tagId") Long tagId,
                                          Pageable pageable);

    // 카테고리 내에서 특정 태그를 가진 북마크 조회
    @Query("SELECT b FROM Bookmark b JOIN b.bookmarkTags bt WHERE b.team.id = :teamId " +
            "AND b.category.id = :categoryId AND bt.tag.id = :tagId")
    Page<Bookmark> findByCategory_IdAndTag_Id(@Param("teamId") Long teamId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("tagId") Long tagId,
                                              Pageable pageable);

    // 북마크 조회 시 북마크-태그와 태그 정보 모두 가져오는 쿼리
    @Query("SELECT b FROM Bookmark b LEFT JOIN FETCH b.bookmarkTags bt LEFT JOIN FETCH bt.tag WHERE b.id = :id")
    Optional<Bookmark> findByIdWithTags(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Bookmark b SET b.category = null WHERE b.category.id = :categoryId")
    void bulkSetCategoryToNull(@Param("categoryId") Long categoryId);
}
