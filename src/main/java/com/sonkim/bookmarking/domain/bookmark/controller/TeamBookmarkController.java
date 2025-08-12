package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그룹 북마크 관리", description = "그룹 내 북마크 생성 및 조회 API")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/groups")
public class TeamBookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "그룹에 북마크 등록", description = "특정 그룹에 새로운 북마크를 등록합니다. EDITOR 이상의 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "북마크 등록 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (VIEWER는 등록 불가)"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 카테고리를 찾을 수 없음")
    })
    @PostMapping("/{groupId}/bookmarks")
    public ResponseEntity<?> createBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long groupId,
                                            @RequestBody BookmarkRequestDto bookmarkRequestDto) {
        log.info("userId: {}, url: {} 북마크 생성 요청", userDetails.getId(), bookmarkRequestDto.getUrl());
        Bookmark bookmark = bookmarkService.createBookmark(userDetails.getId(), groupId, bookmarkRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("bookmarkId: " + bookmark.getId() + " created");
    }

    @Operation(summary = "그룹 내 모든 북마크 조회 (페이징)", description = "특정 그룹에 속한 모든 북마크를 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    @GetMapping("/{groupId}/bookmarks")
    public ResponseEntity<?> getBookmarksOfGroup(@PathVariable("groupId") Long groupId,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long tagId,
                                                 @PageableDefault(sort = "createdAt") Pageable pageable) {

        PageResponseDto<BookmarkResponseDto> bookmarkList;

        if (tagId != null) {
            bookmarkList = bookmarkService.getBookmarksByTagInGroup(groupId, tagId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            bookmarkList = bookmarkService.searchBookmarksByTeamId(groupId, keyword, pageable);
        } else {
            bookmarkList = bookmarkService.getBookmarksByTeamId(groupId, pageable);
        }

        return ResponseEntity.ok(bookmarkList);
    }

    @Operation(summary = "특정 카테고리 내 북마크 조회 (페이징)", description = "특정 그룹의 특정 카테고리에 속한 북마크를 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 카테고리를 찾을 수 없음")
    })
    @GetMapping("/{groupId}/categories/{categoryId}/bookmarks")
    public ResponseEntity<?> getBookmarksOfCategory(@PathVariable("groupId") Long groupId,
                                                    @PathVariable("categoryId") Long categoryId,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Long tagId,
                                                    @PageableDefault(sort = "createdAt") Pageable pageable) {
        PageResponseDto<BookmarkResponseDto> bookmarkList;

        if (tagId != null) {
            bookmarkList = bookmarkService.getBookmarksByTagInCategory(groupId, categoryId, tagId, pageable);
        }else if (keyword != null && !keyword.isBlank()) {
            bookmarkList = bookmarkService.searchBookmarksByTeamIdAndCategoryId(groupId, categoryId, keyword, pageable);
        } else {
            bookmarkList = bookmarkService.getBookmarksByTeamIdAndCategoryId(groupId, categoryId, pageable);
        }

        return ResponseEntity.ok(bookmarkList);
    }
}
