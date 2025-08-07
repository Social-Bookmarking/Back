package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkLikeService;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import com.sonkim.bookmarking.util.OGUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final BookmarkLikeService bookmarkLikeService;

    // 특정 북마크 상세 정보 조회
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<?> getBookmark(@PathVariable("bookmarkId") Long bookmarkId) {
        Bookmark bookmark = bookmarkService.getBookmarkById(bookmarkId);
        Long likesCount = bookmarkLikeService.CountBookmarkLike(bookmarkId);

        return ResponseEntity.ok(BookmarkResponseDto.fromEntityWithLikes(bookmark, likesCount));
    }

    // 북마크 정보 수정
    @PatchMapping("/{bookmarkId}")
    public ResponseEntity<?> updateBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("bookmarkId") Long bookmarkId,
                                            @RequestBody BookmarkRequestDto bookmarkRequestDto) {
        log.info("userId: {}, bookmarkId: {} 북마크 수정 요청", userDetails.getId(), bookmarkId);
        bookmarkService.updateBookmark(userDetails.getId(), bookmarkId, bookmarkRequestDto);

        return ResponseEntity.ok("bookmarkId: " + bookmarkId + " updated");
    }

    // 북마크 삭제
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<?> deleteBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 북마크 삭제 요청", userDetails.getId(), bookmarkId);
        bookmarkService.deleteBookmark(userDetails.getId(), bookmarkId);

        return ResponseEntity.ok("bookmarkId: " + bookmarkId + " deleted");
    }

    // OpenGraph 정보 추출
    @GetMapping("/og-info")
    public ResponseEntity<?> getBookmarkInformation(@RequestParam String url) {
        try {
            BookmarkOGDto info = OGUtil.getOpenGraphData(url);

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("정보 추출에 실패했습니다.");
        }
    }
}
