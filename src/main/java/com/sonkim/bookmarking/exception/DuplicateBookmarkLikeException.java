package com.sonkim.bookmarking.exception;

public class DuplicateBookmarkLikeException extends RuntimeException {
    public DuplicateBookmarkLikeException() {
        super("이미 '좋아요'를 누른 북마크입니다.");
    }
}
