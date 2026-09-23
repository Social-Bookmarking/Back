package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.s3.dto.PresignedUrlDto;
import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkUpdateDto;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.common.util.OpenGraphImageCache;
import com.sonkim.bookmarking.common.util.RemoteImageUrlValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
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
    private final OGUtil ogUtil;
    private final S3Service s3Service;
    private final OpenGraphImageCache openGraphImageCache;

    @Operation(summary = "특정 북마크 상세 정보 조회", description = "특정 북마크의 상세 정보와 좋아요 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 조회 성공"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<BookmarkResponseDto> getBookmark(
            @PathVariable("bookmarkId") Long bookmarkId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        BookmarkResponseDto responseDto = bookmarkService.getBookmarkDetails(userDetails.getId(), bookmarkId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "북마크 정보 수정", description = "북마크의 제목, 설명, 카테고리 등을 수정합니다. 북마크 생성자만 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (생성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @PatchMapping("/{bookmarkId}")
    public ResponseEntity<String> updateBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("bookmarkId") Long bookmarkId,
                                            @RequestBody @Valid BookmarkUpdateDto bookmarkRequestDto) {
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
    public ResponseEntity<Void> deleteBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails,
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
    public ResponseEntity<BookmarkOGDto> getBookmarkInformation(@RequestParam String url) {
        try {
            BookmarkOGDto info = ogUtil.getOpenGraphData(url);
            if (info.getImage() != null && !info.getImage().isBlank()) {
                try {
                    openGraphImageCache.put(url, RemoteImageUrlValidator.validate(info.getImage()));
                } catch (IllegalArgumentException e) {
                    log.warn("OpenGraph 이미지 URL을 캐싱하지 않습니다. pageUrl={}, imageUrl={}",
                            url, info.getImage(), e);
                }
            }
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            // 🔽 🚨 중요: 발생한 예외(e)를 함께 로깅하여 원인을 파악합니다.
            log.error("OG 정보 추출 중 컨트롤러에서 에러 발생. URL: {}", url, e);

            // GlobalExceptionHandler가 처리하도록 예외를 다시 던집니다.
            throw new RuntimeException("정보 추출 중 서버 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "북마크 이미지 업로드를 위한 Pre-signed URL 발급")
    @GetMapping("/upload-url")
    public ResponseEntity<PresignedUrlDto> getBookmarkPreSignedUrl(
            @RequestParam String fileName
    ) {
        PresignedUrlDto presignedUrlDto = s3Service.generatePresignedPutUrl(fileName);
        return ResponseEntity.ok(presignedUrlDto);
    }
}
