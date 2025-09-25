package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.aop.Idempotent;
import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkSearchCond;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.Map;

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
    @Idempotent
    public ResponseEntity<Map<String, Object>> createBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long groupId,
                                            @RequestBody BookmarkRequestDto bookmarkRequestDto) {
        log.info("userId: {}, url: {} 북마크 생성 요청", userDetails.getId(), bookmarkRequestDto.getUrl());
        Bookmark bookmark = bookmarkService.createBookmark(userDetails.getId(), groupId, bookmarkRequestDto);

        Map<String, Object> response = Map.of(
                "message", "Bookmark created successfully",
                "bookmarkId", bookmark.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 내 모든 북마크 조회 (페이징)",
            description = "특정 그룹에 속한 모든 북마크를 페이징하여 조회합니다.",
            parameters = {
                    @Parameter(name = "page", description = "표시할 페이지 (1부터 시작)")
            })
    @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    @GetMapping("/{groupId}/bookmarks")
    public ResponseEntity<PageResponseDto<BookmarkResponseDto>> getBookmarksOfGroup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("groupId") Long groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt") Pageable pageable) {
        // 검색 조건 DTO 생성
        BookmarkSearchCond cond = BookmarkSearchCond.builder()
                .keyword(keyword)
                .tagId(tagId)
                .categoryId(categoryId)
                .build();

        PageResponseDto<BookmarkResponseDto> bookmarkPage = bookmarkService.searchBookmarks(userDetails.getId(), groupId, cond, pageable);
        return ResponseEntity.ok(bookmarkPage);
    }

    @Operation(summary = "지도 표시용 북마크 목록 조회 (필터링, 페이징)",
            description = "특정 그룹에서 위치 정보가 있는 북마크를 조건(카테고리, 키워드, 태그)에 따라 필터링하여 조회합니다.",
            parameters = {
                    @Parameter(name = "page", description = "표시할 페이지 (1부터 시작)")
            })
    @GetMapping("/{groupId}/bookmarks/map")
    public ResponseEntity<PageResponseDto<BookmarkResponseDto>> getBookmarksForMap(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "북마크를 조회할 그룹 ID") @PathVariable("groupId") Long groupId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable
    ) {
        // 검색 조건 DTO 생성
        BookmarkSearchCond cond = BookmarkSearchCond.builder()
                .keyword(keyword)
                .tagId(tagId)
                .categoryId(categoryId)
                .forMap(true)
                .build();

        PageResponseDto<BookmarkResponseDto> bookmarks = bookmarkService.searchBookmarks(userDetails.getId(), groupId, cond, pageable);
        return ResponseEntity.ok(bookmarks);
    }
}
