package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.service.AccountService;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkLike;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkLikeRepository;
import com.sonkim.bookmarking.exception.DuplicateBookmarkLikeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookmarkLikeService {

    private final BookmarkService bookmarkService;
    private final AccountService accountService;
    private final BookmarkLikeRepository bookmarkLikeRepository;

    // 북마크-좋아요 수 조회
    @Transactional(readOnly = true)
    public Long CountBookmarkLike(Long bookmarkId) {
        return bookmarkLikeRepository.countBookmarkLikesByBookmark_Id(bookmarkId);
    }

    // 북마크-좋아요 추가
    @Transactional
    public void createBookmarkLike(Long accountId, Long bookmarkId) {
        // 중복 체크
        if(bookmarkLikeRepository.existsBookmarkLikeByAccount_IdAndBookmark_Id(accountId, bookmarkId)) {
            throw new DuplicateBookmarkLikeException();
        }

        Account account = accountService.getAccountById(accountId);
        Bookmark bookmark = bookmarkService.getBookmarkById(bookmarkId);

        BookmarkLike bookmarkLike = BookmarkLike.builder()
                .bookmark(bookmark)
                .account(account)
                .build();

        bookmarkLikeRepository.save(bookmarkLike);
    }

    // 북마크-좋아요 취소
    @Transactional
    public void deleteBookmarkLike(Long accountId, Long bookmarkId) {
        // 좋아요 정보가 존재하는 경우 삭제 수행
        // 좋아요가 없을 때 삭제 요청이 와도 이미 없다는 목표가 달성되었으므로 예외 발생 x
        bookmarkLikeRepository.findByAccount_IdAndBookmark_Id(accountId, bookmarkId)
                .ifPresent(bookmarkLikeRepository::delete);
    }
}
