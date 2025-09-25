package com.sonkim.bookmarking.domain.category.repository;

import com.sonkim.bookmarking.domain.category.dto.CategoryDto;

import java.util.List;

public interface CategoryRepositoryCustom {
    // 카테고리 정보와 해당 카테고리에 속한 북마크의 개수를 카운트
    List<CategoryDto.CategoryResponseDto> findAllWithBookmarkCountByTeam_Id(Long teamId);
}
