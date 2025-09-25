package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkTag;

import java.util.List;

public interface BookmarkTagRepositoryCustom {
    List<BookmarkTag> findAllByBookmarkIdsWithTags(List<Long> bookmarkIds);
}
