package com.sonkim.bookmarking.domain.bookmark.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkTag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.sonkim.bookmarking.domain.bookmark.entity.QBookmarkTag.bookmarkTag;
import static com.sonkim.bookmarking.domain.tag.entity.QTag.tag;

@RequiredArgsConstructor
public class BookmarkTagRepositoryCustomImpl implements BookmarkTagRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookmarkTag> findAllByBookmarkIdsWithTags(List<Long> bookmarkIds) {
        return queryFactory
                .selectFrom(bookmarkTag)
                .join(bookmarkTag.tag, tag).fetchJoin()
                .where(bookmarkTag.bookmark.id.in(bookmarkIds))
                .fetch();
    }
}
