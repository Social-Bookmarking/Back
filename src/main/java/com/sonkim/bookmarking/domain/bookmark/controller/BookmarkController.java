package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import com.sonkim.bookmarking.common.util.OGUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "북마크 관리", description = "개별 북마크 조회, 수정, 삭제 및 OpenGraph 정보 추출 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "특정 북마크 상세 정보 조회", description = "특정 북마크의 상세 정보와 좋아요 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 조회 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<?> getBookmark(@PathVariable("bookmarkId") Long bookmarkId) {
        BookmarkResponseDto responseDto = bookmarkService.getBookmarkDtoById(bookmarkId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "북마크 정보 수정", description = "북마크의 제목, 설명, 카테고리 등을 수정합니다. 북마크 생성자만 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (생성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @PatchMapping("/{bookmarkId}")
    public ResponseEntity<?> updateBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("bookmarkId") Long bookmarkId,
                                            @RequestBody BookmarkRequestDto bookmarkRequestDto) {
        log.info("userId: {}, bookmarkId: {} 북마크 수정 요청", userDetails.getId(), bookmarkId);
        bookmarkService.updateBookmark(userDetails.getId(), bookmarkId, bookmarkRequestDto);
        return ResponseEntity.ok("bookmarkId: " + bookmarkId + " updated");
    }

    @Operation(summary = "북마크 삭제", description = "북마크를 삭제합니다. 북마크 생성자 또는 그룹의 ADMIN만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<?> deleteBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("bookmarkId") Long bookmarkId) {
        log.info("userId: {}, bookmarkId: {} 북마크 삭제 요청", userDetails.getId(), bookmarkId);
        bookmarkService.deleteBookmark(userDetails.getId(), bookmarkId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "URL의 OpenGraph 정보 추출", description = "북마크로 등록할 URL을 보내면 해당 페이지의 제목, 설명, 대표 이미지(OG 정보)를 미리 추출하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정보 추출 성공"),
            @ApiResponse(responseCode = "404", description = "정보를 추출할 수 없거나 유효하지 않은 URL")
    })
    @GetMapping("/og-info")
    public ResponseEntity<?> getBookmarkInformation(@RequestParam String url) {
        try {
            BookmarkOGDto info = OGUtil.getOpenGraphData(url);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            throw new EntityNotFoundException("정보 추출에 실패했습니다.");
        }
    }
}
