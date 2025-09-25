package com.sonkim.bookmarking.domain.bookmark.repository;

import jakarta.persistence.EntityManager;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkSearchCond;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.category.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.sonkim.bookmarking.domain.bookmark.entity.QBookmark.bookmark;
import static com.sonkim.bookmarking.domain.bookmark.entity.QBookmarkTag.bookmarkTag;
import static com.sonkim.bookmarking.domain.tag.entity.QTag.tag;
import static com.sonkim.bookmarking.domain.bookmark.entity.QBookmarkLike.bookmarkLike;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Page<Bookmark> search(Long teamId, BookmarkSearchCond cond, Pageable pageable) {
        List<Bookmark> content = queryFactory
                .select(bookmark)
                .from(bookmark)
                .leftJoin(bookmark.bookmarkTags, bookmarkTag)
                .where(
                        bookmark.team.id.eq(teamId),
                        keywordContain(cond.getKeyword()),
                        tagIdEq(cond.getTagId()),
                        categoryIdEq(cond.getCategoryId()),
                        locationIsNotNull(cond.getForMap())
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(bookmark.countDistinct())
                .from(bookmark)
                .leftJoin(bookmark.bookmarkTags, bookmarkTag)
                .where(
                        bookmark.team.id.eq(teamId),
                        keywordContain(cond.getKeyword()),
                        tagIdEq(cond.getCategoryId()),
                        categoryIdEq(cond.getCategoryId()),
                        locationIsNotNull(cond.getForMap())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Bookmark> findByIdWithTags(Long id) {
        Bookmark result = queryFactory
                .select(bookmark)
                .from(bookmark)
                .leftJoin(bookmark.bookmarkTags, bookmarkTag).fetchJoin()
                .leftJoin(bookmarkTag.tag, tag).fetchJoin()
                .where(bookmark.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void bulkSetCategoryToNull(Long categoryId) {
        long affectedRows = queryFactory
                .update(bookmark)
                .set(bookmark.category, (Category) null)
                .where(bookmark.category.id.eq(categoryId))
                .execute();

        em.flush();
        em.clear();
    }

    @Override
    public Page<Bookmark> findLikedBookmarksByUser_Id(Long userId, Pageable pageable) {
        List<Bookmark> content = queryFactory
                .select(bookmark)
                .from(bookmark)
                .join(bookmark.bookmarkLikes, bookmarkLike)
                .where(bookmarkLike.user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(bookmark.count())
                .from(bookmark)
                .join(bookmark.bookmarkLikes, bookmarkLike)
                .where(bookmarkLike.user.id.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression locationIsNotNull(boolean forMap) {
        return forMap ? bookmark.latitude.isNotNull().and(bookmark.longitude.isNotNull()) : null;
    }

    private BooleanExpression keywordContain(String keyword) {
        return keyword != null ? bookmark.title.lower().contains(keyword.toLowerCase())
                .or(bookmark.description.lower().contains(keyword.toLowerCase())) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? bookmark.category.id.eq(categoryId) : null;
    }

    private BooleanExpression tagIdEq(Long tagId) {
        return tagId != null ? bookmarkTag.tag.id.eq(tagId) : null;
    }
}
