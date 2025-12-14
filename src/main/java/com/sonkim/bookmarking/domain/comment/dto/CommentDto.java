package com.sonkim.bookmarking.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class CommentDto {

    // 댓글, 답글 등록 DTO
    @Data
    public static class CreateRequestDto {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 100, message = "댓글은 100자를 초과할 수 없습니다.")
        private String content;

        private Long parentId;  // 최상위 댓글이면 null, 답글이면 부모 댓글 ID
    }

    // 최상위 댓글 목록 조회 DTO
    @Data
    @Builder
    public static class TopLevelResponseDto {
        private Long commentId;
        private String content;
        private AuthorInfo author;
        private LocalDateTime createdAt;
        private int replyCount;
    }

    // 답글 목록 조회 DTO
    @Data
    @Builder
    public static class ReplyResponseDto {
        private Long commentId;
        private String content;
        private AuthorInfo author;
        private LocalDateTime createdAt;
        private String parentAuthorNickname;
    }

    // 작성자 정보
    @Data
    @Builder
    public static class AuthorInfo {
        private Long userId;
        private String nickname;
        private String profileImageUrl;
    }

    // 댓글/답글 생성 직후 반환할 DTO
    @Data
    @Builder
    public static class CreateResponseDto {
        private Long commentId;
        private String content;
        private AuthorInfo author;
        private LocalDateTime createdAt;
        private int replyCount;
        private Long parentId;
        private String parentAuthorNickname;
    }
}
