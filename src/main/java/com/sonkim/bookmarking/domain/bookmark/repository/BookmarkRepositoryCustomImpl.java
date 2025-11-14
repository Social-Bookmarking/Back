package com.sonkim.bookmarking.domain.bookmark.repository;

import com.querydsl.core.types.Projections;
import com.sonkim.bookmarking.domain.bookmark.dto.LikedBookmarkWrapper;
import jakarta.persistence.EntityManager;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkSearchCond;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.category.entity.Category;
import lombok.RequiredArgsConstructor;

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
    public List<Bookmark> search(Long teamId, BookmarkSearchCond cond, Long cursorId, int size) {
        List<Bookmark> content = queryFactory
                .select(bookmark)
                .from(bookmark)
                .leftJoin(bookmark.bookmarkTags, bookmarkTag)
                .where(
                        bookmark.team.id.eq(teamId),
                        keywordContain(cond.getKeyword()),
                        tagIdEq(cond.getTagId()),
                        categoryIdEq(cond.getCategoryId()),
                        locationIsNotNull(cond.getForMap()),
                        cursorIdLessThen(cursorId)
                )
                .distinct()
                .limit(size)
                .orderBy(bookmark.id.desc())
                .fetch();

        return content;
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
    public List<Bookmark> findMyBookmarksByUser_Id(Long userId, Long cursorId, int size) {
        return queryFactory
                .selectFrom(bookmark)
                .where(
                        bookmark.user.id.eq(userId),
                        cursorIdLessThen(cursorId)
                )
                .orderBy(bookmark.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<LikedBookmarkWrapper> findLikedBookmarksByUser_Id(Long userId, Long cursorId, int size) {
        return queryFactory
                .select(Projections.constructor(LikedBookmarkWrapper.class,
                        bookmark,
                        bookmarkLike.id
                ))
                .from(bookmarkLike)
                .join(bookmarkLike.bookmark, bookmark)
                .where(
                        bookmarkLike.user.id.eq(userId),
                        bookmarkLikedCursorIdLessThen(cursorId)
                )
                .orderBy(bookmarkLike.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression locationIsNotNull(Boolean forMap) {
        return forMap!=null && forMap ? bookmark.latitude.isNotNull().and(bookmark.longitude.isNotNull()) : null;
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

    private BooleanExpression cursorIdLessThen(Long cursorId) {
        return cursorId != null ? bookmark.id.lt(cursorId) : null;
    }

    private BooleanExpression bookmarkLikedCursorIdLessThen(Long cursorId) {
        return cursorId != null ? bookmarkLike.id.lt(cursorId) : null;
    }
}
