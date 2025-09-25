package com.sonkim.bookmarking.domain.category.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.sonkim.bookmarking.domain.category.entity.QCategory.category;
import static com.sonkim.bookmarking.domain.bookmark.entity.QBookmark.bookmark;

@RequiredArgsConstructor
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CategoryDto.CategoryResponseDto> findAllWithBookmarkCountByTeam_Id(Long teamId) {
        return queryFactory
                .select(Projections.constructor(CategoryDto.CategoryResponseDto.class,
                        category.id,
                        category.name,
                        bookmark.id.count()
                ))
                .from(category)
                .leftJoin(bookmark).on(bookmark.category.id.eq(category.id))
                .where(category.team.id.eq(teamId))
                .groupBy(category.id, category.name)
                .fetch();
    }
}
