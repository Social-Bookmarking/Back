package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/bookmarks/{bookmarkId}/like")
public class BookmarkLikeController {

    private final BookmarkLikeService bookmarkLikeService;

    // 좋아요 추가
    @PostMapping
    public ResponseEntity<?> addLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 좋아요 추가", userDetails.getId(), bookmarkId);
        bookmarkLikeService.createBookmarkLike(userDetails.getId(), bookmarkId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 좋아요 삭제
    @DeleteMapping
    public ResponseEntity<?> removeLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 좋아요 삭제", userDetails.getId(), bookmarkId);
        bookmarkLikeService.deleteBookmarkLike(userDetails.getId(), bookmarkId);

        return ResponseEntity.noContent().build();
    }
}
