package com.sonkim.bookmarking.domain.comment.service;

import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.comment.dto.CommentDto;
import com.sonkim.bookmarking.domain.comment.dto.CommentReplyCountDto;
import com.sonkim.bookmarking.domain.comment.entity.Comment;
import com.sonkim.bookmarking.domain.comment.repository.CommentRepository;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookmarkService bookmarkService;
    private final TeamMemberService teamMemberService;

    // 댓글 등록
    @Transactional
    public void createComment(Long userId, Long bookmarkId, CommentDto.CreateRequestDto request) {
        log.info("userId: {}, bookmarkId: {} 댓글 등록 요청", userId, bookmarkId);
        User user = userService.getUserById(userId);
        Bookmark bookmark = bookmarkService.getBookmarkById(bookmarkId);
        Comment parent = null;

        // 답글인 경우 부모 댓글 설정
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
        }

        Comment newComment = Comment.builder()
                .user(user)
                .bookmark(bookmark)
                .parent(parent)
                .content(request.getContent())
                .build();

        commentRepository.save(newComment);
    }

    // 최상위 댓글 목록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponseDto<CommentDto.TopLevelResponseDto> getTopLevelComments(Long bookmarkId, Pageable pageable) {
        // 페이징된 최상위 댓글 목록 조회
        Page<Comment> comments = commentRepository.findByBookmark_IdAndParentIsNullOrderByCreatedAtAsc(bookmarkId, pageable);
        List<Long> commentIds = comments.getContent().stream().map(Comment::getId).toList();

        // 재귀 쿼리로 답글 수 조회
        Map<Long, Integer> replyCountMap = Collections.emptyMap();
        if (!commentIds.isEmpty()) {
            replyCountMap = commentRepository.countRepliesForParents(commentIds).stream()
                    .collect(Collectors.toMap(CommentReplyCountDto::getCommentId, CommentReplyCountDto::getReplyCount));
        }

        final Map<Long, Integer> finalReplyCountMap = replyCountMap;

        // Page<CommentDto.TopLevelResponseDto> 로 변환
        Page<CommentDto.TopLevelResponseDto> dtoPage = comments.map(comment -> CommentDto.TopLevelResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .author(getAuthorInfo(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .replyCount(finalReplyCountMap.getOrDefault(comment.getId(), 0))
                .build());

        // PageResponseDto로 변환
        return new PageResponseDto<>(dtoPage);
    }

    // 댓글에 달린 답글 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDto<CommentDto.ReplyResponseDto> getReplyComments(Long parentCommentId, Pageable pageable) {
        // 답글 목록 가져오기
        List<Comment> replies = commentRepository.findDescendantsPaged(parentCommentId, pageable);

        // 답글 개수 조회
        long totalReplies = commentRepository.countDescendants(parentCommentId);

        // List<ReplyResponseDto>로 변환
        List<CommentDto.ReplyResponseDto> dtoList = replies.stream()
                .map(reply -> CommentDto.ReplyResponseDto.builder()
                        .commentId(reply.getId())
                        .content(reply.getContent())
                        .createdAt(reply.getCreatedAt())
                        .author(getAuthorInfo(reply.getUser()))
                        .parentAuthorNickname(reply.getParent().getUser().getProfile().getNickname())
                        .build())
                .toList();

        // 조회된 데이터, 페이징 정보, 전체 개수를 합쳐 Page 객체 생성
        Page<CommentDto.ReplyResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, totalReplies);

        // DTO로 변환
        return new PageResponseDto<>(dtoPage);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("userId: {}, commentId: {} 삭제 요청", userId, commentId);

        Comment comment = commentRepository.findCommentWithChildrenById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        Long teamId = comment.getBookmark().getTeam().getId();

        // 작성자인지 확인
        boolean isCreator = comment.getUser().getId().equals(userId);

        // 댓글 작성자 본인과 관리자만 삭제 가능
        if (!isCreator && teamMemberService.validateAdmin(userId, teamId)) {
            throw new AuthorizationDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        // 답글이 하나라도 있으면 내용만 변경, 없으면 완전 삭제
        if (comment.getChildren().isEmpty()) {
            log.info("답글이 없는 댓글(ID: {})을 DB에서 완전히 삭제", commentId);
            commentRepository.delete(comment);
        } else {
            log.info("답글이 있는 댓글(ID: {})을 논리적으로 삭제", commentId);
            comment.softDelete();
        }
    }

    // 댓글 작성자 정보 추출 메서드
    private CommentDto.AuthorInfo getAuthorInfo(User user) {
        return CommentDto.AuthorInfo.builder()
                .userId(user.getId())
                .nickname(user.getProfile().getNickname())
                .profileImageUrl(user.getProfile().getImageKey())
                .build();
    }
}
