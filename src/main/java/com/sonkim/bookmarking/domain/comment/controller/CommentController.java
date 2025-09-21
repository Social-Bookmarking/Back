package com.sonkim.bookmarking.domain.comment.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.aop.Idempotent;
import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.comment.dto.CommentDto;
import com.sonkim.bookmarking.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "댓글 관리", description = "북마크에 대한 댓글 생성, 조회, 삭제 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글/답글 생성", description = "특정 북마크에 새로운 댓글 또는 답글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "댓글 생성 성공")
    @PostMapping("/bookmarks/{bookmarkId}/comments")
    @Idempotent
    public ResponseEntity<Void> createComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "댓글/답글을 작성할 북마크 ID") @PathVariable("bookmarkId") Long bookmarkId,
            @RequestBody CommentDto.CreateRequestDto request) {
        commentService.createComment(userDetails.getId(), bookmarkId, request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "최상위 댓글 목록 조회 (페이징)", description = "특정 북마크의 최상위 댓글 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    @GetMapping("/bookmarks/{bookmarkId}/comments")
    public ResponseEntity<PageResponseDto<CommentDto.TopLevelResponseDto>> getTopLevelComments(
            @Parameter(description = "댓글을 조회할 북마크 ID") @PathVariable("bookmarkId") Long bookmarkId,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt") Pageable pageable) {
        PageResponseDto<CommentDto.TopLevelResponseDto> comments = commentService.getTopLevelComments(bookmarkId, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "답글 목록 조회(페이징)", description = "특정 댓글에 달린 모든 답글을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "답글 목록 조회 성공")
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<PageResponseDto<CommentDto.ReplyResponseDto>> getReplies(
            @Parameter(description = "답글을 조회할 댓글 ID") @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        PageResponseDto<CommentDto.ReplyResponseDto> replies = commentService.getReplyComments(commentId, pageable);
        return ResponseEntity.ok(replies);
    }

    @Operation(summary = "댓글/답글 삭제", description = "특정 댓글 또는 답글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "삭제할 댓글 ID") @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.noContent().build();
    }

}
