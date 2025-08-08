package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/groups")
public class TeamBookmarkController {

    private final BookmarkService bookmarkService;

    // 북마크 등록
    @PostMapping("/{groupId}/bookmarks")
    public ResponseEntity<?> createBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long groupId,
                                            @RequestBody BookmarkRequestDto bookmarkRequestDto) {
        log.info("userId: {}, url: {} 북마크 생성 요청", userDetails.getId(), bookmarkRequestDto.getUrl());
        Bookmark bookmark = bookmarkService.createBookmark(userDetails.getId(), groupId, bookmarkRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("bookmarkId: " + bookmark.getId() + " created");
    }

    // 그룹 내 모든 북마크 조회
    @GetMapping("/{groupId}/bookmarks")
    public ResponseEntity<?> getBookmarksOfGroup(@PathVariable("groupId") Long groupId) {
        List<Bookmark> bookmarkList = bookmarkService.getBookmarksByTeamId(groupId);

        return ResponseEntity.ok(bookmarkList);
    }

    // 특정 카테고리 북마크 조회
    @GetMapping("/{groupId}/categories/{categoryId}/bookmarks")
    public ResponseEntity<?> getBookmarksOfCategory(@PathVariable("groupId") Long groupId,
                                                    @PathVariable("categoryId") Long categoryId) {
        List<Bookmark> bookmarkList = bookmarkService.getBookmarksByTeamIdAndCategoryId(groupId, categoryId);

        return ResponseEntity.ok(bookmarkList);
    }


}
