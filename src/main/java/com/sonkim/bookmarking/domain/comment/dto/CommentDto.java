package com.sonkim.bookmarking.domain.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class CommentDto {

    // 댓글, 답글 등록 DTO
    @Data
    public static class CreateRequestDto {
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
}
