package com.sonkim.bookmarking.domain.bookmark.repository;

import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkTagRepository extends JpaRepository<BookmarkTag, Long>, BookmarkTagRepositoryCustom {
}
