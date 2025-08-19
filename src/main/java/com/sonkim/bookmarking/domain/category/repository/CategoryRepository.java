package com.sonkim.bookmarking.domain.category.repository;

import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndTeam_Id(String name, Long teamId);

    // 카테고리 정보와 해당 카테고리에 속한 북마크의 개수를 카운트
    @Query("SELECT new com.sonkim.bookmarking.domain.category.dto.CategoryDto$CategoryResponseDto(c.id, c.name, COUNT(b.id)) " +
            "FROM Category c LEFT JOIN Bookmark b ON b.category.id = c.id " +
            "WHERE c.team.id = :teamId " +
            "GROUP BY c.id, c.name")
    List<CategoryDto.CategoryResponseDto> findAllWithBookmarkCountByTeam_Id(@Param("teamId") Long teamId);
}
