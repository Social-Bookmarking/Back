package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "북마크 좋아요 관리", description = "북마크에 대한 '좋아요' 추가 및 삭제 API")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/bookmarks/{bookmarkId}/like")
public class BookmarkLikeController {

    private final BookmarkLikeService bookmarkLikeService;

    @Operation(summary = "북마크에 '좋아요' 추가", description = "사용자가 특정 북마크에 '좋아요'를 누릅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "'좋아요' 추가 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 '좋아요'를 누른 북마크")
    })
    @PostMapping
    public ResponseEntity<Void> addLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 좋아요 추가", userDetails.getId(), bookmarkId);
        bookmarkLikeService.createBookmarkLike(userDetails.getId(), bookmarkId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "북마크 '좋아요' 취소", description = "사용자가 특정 북마크에 눌렀던 '좋아요'를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "'좋아요' 취소 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @DeleteMapping
    public ResponseEntity<Void> removeLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 좋아요 삭제", userDetails.getId(), bookmarkId);
        bookmarkLikeService.deleteBookmarkLike(userDetails.getId(), bookmarkId);

        return ResponseEntity.noContent().build();
    }
}
