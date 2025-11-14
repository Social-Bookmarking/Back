package com.sonkim.bookmarking.domain.comment.service;

import com.sonkim.bookmarking.common.dto.CursorResultDto;
import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.comment.dto.CommentDto;
import com.sonkim.bookmarking.domain.comment.dto.CommentReplyCountDto;
import com.sonkim.bookmarking.domain.comment.entity.Comment;
import com.sonkim.bookmarking.domain.comment.enums.CommentStatus;
import com.sonkim.bookmarking.domain.comment.repository.CommentRepository;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
    private final S3Service s3Service;

    // 댓글 등록
    @Transactional
    public CommentDto.CreateResponseDto createComment(Long userId, Long bookmarkId, CommentDto.CreateRequestDto request) {
        log.info("userId: {}, bookmarkId: {} 댓글 등록 요청", userId, bookmarkId);
        User user = userService.getUserById(userId);
        Bookmark bookmark = bookmarkService.getBookmarkById(bookmarkId);

        Comment parent = null;
        String parentAuthorNickname = null;
        Long parentId = null;

        // 답글인 경우 부모 댓글 설정
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));

            parentAuthorNickname = parent.getUser().getProfile().getNickname();
            parentId = parent.getId();
        }

        Comment newComment = Comment.builder()
                .user(user)
                .bookmark(bookmark)
                .content(request.getContent())
                .parent(parent)
                .build();

        commentRepository.save(newComment);

        return CommentDto.CreateResponseDto.builder()
                .commentId(newComment.getId())
                .content(newComment.getContent())
                .author(getAuthorInfo(newComment.getUser()))
                .createdAt(newComment.getCreatedAt())
                .replyCount(0)
                .parentId(parentId)
                .parentAuthorNickname(parentAuthorNickname)
                .build();
    }

    // 최상위 댓글 목록 페이징 조회
    @Transactional(readOnly = true)
    public CursorResultDto<CommentDto.TopLevelResponseDto> getTopLevelComments(Long bookmarkId, Long cursorId, int size) {
        // 다음 페이지 존재 여부 확인을 위해 요청된 size보다 1개 더 조회
        List<Comment> comments = commentRepository.findTopLevelCommentsAfterCursor(
                bookmarkId, cursorId, PageRequest.of(0, size + 1)
        );

        boolean hasNext = false;
        if (comments.size() > size) {
            hasNext = true;
            comments.remove(size);
        }

        // 재귀 쿼리로 답글 수 조회
        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        Map<Long, Integer> replyCountMap = Collections.emptyMap();
        if (!commentIds.isEmpty()) {
            replyCountMap = commentRepository.countRepliesForParents(commentIds).stream()
                    .collect(Collectors.toMap(CommentReplyCountDto::getCommentId, CommentReplyCountDto::getReplyCount));
        }

        final Map<Long, Integer> finalReplyCountMap = replyCountMap;

        // List<CommentDto.TopLevelResponseDto> 로 변환
        List<CommentDto.TopLevelResponseDto> dtoList = comments.stream()
                .map(comment -> CommentDto.TopLevelResponseDto.builder()
                        .commentId(comment.getId())
                        .content(comment.getContent())
                        .author(getAuthorInfo(comment.getUser()))
                        .createdAt(comment.getCreatedAt())
                        .replyCount(finalReplyCountMap.getOrDefault(comment.getId(), 0))
                        .build())
                .toList();

        // 다음 커서 ID 설정
        Long nextCursor = dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).getCommentId();

        return new CursorResultDto<>(dtoList, nextCursor, hasNext);
    }

    // 댓글에 달린 답글 목록 조회
    @Transactional(readOnly = true)
    public CursorResultDto<CommentDto.ReplyResponseDto> getReplyComments(Long parentCommentId, Long cursorId, int size) {
        // 다음 페이지 확인을 위해 size + 1개 조회
        List<Comment> replies = commentRepository.findDescendantsPaged(parentCommentId, cursorId, size + 1);

        boolean hasNext = false;
        if (replies.size() > size) {
            hasNext = true;
            replies.remove(size);
        }

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
        Long nextCursor = dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).getCommentId();

        // DTO로 변환
        return new CursorResultDto<>(dtoList, nextCursor, hasNext);
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
        if (!isCreator) {
            teamMemberService.validateAdmin(userId, teamId);
        }

        // 최상위 댓글 OR 답글 분기
        if (comment.getParent() == null) {
            // 최상위 댓글인 경우 -> 전체 스레드 삭제
            log.info("최상위 댓글(ID: {})과 모든 하위 답글을 DB에서 완전히 삭제", commentId);
            commentRepository.delete(comment);
        } else {
            // 답글이 하나라도 있으면 내용만 변경, 없으면 완전 삭제
            if (comment.getChildren().isEmpty()) {
                Comment parent = comment.getParent();

                // 부모 댓글의 답글 리스트에서 삭제
                parent.getChildren().remove(comment);

                log.info("답글이 없는 댓글(ID: {})을 DB에서 완전히 삭제", commentId);
                commentRepository.delete(comment);

                // 부모 댓글이 있었으면 부모도 정리해야 하는지 확인
                cleanupParentIfOrphaned(parent);
            } else {
                log.info("답글이 있는 댓글(ID: {})을 논리적으로 삭제", commentId);
                comment.softDelete();
            }
        }
    }

    @Transactional
    protected void cleanupParentIfOrphaned(Comment parent) {
        // 부모 댓글이 삭제된 상태이고 자식이 하나도 없는지 확인
        if (parent.getStatus() == CommentStatus.DELETED && parent.getChildren().isEmpty()) {
            log.info("삭제된 부모 댓글(ID: {})에 등록된 답글이 없으므로 함께 삭제", parent.getId());

            // 조부모 댓글도 확인 수행
            Comment grandParent = parent.getParent();

            if (grandParent != null) {
                grandParent.getChildren().remove(parent);
            }

            commentRepository.delete(parent);

            if (grandParent != null) {
                cleanupParentIfOrphaned(grandParent);
            }
        }
    }

    // 댓글 작성자 정보 추출 메서드
    private CommentDto.AuthorInfo getAuthorInfo(User user) {
        String imageUrl = null;
        String imageKey = user.getProfile().getImageKey();

        if (imageKey != null && !imageKey.isBlank()) {
            imageUrl = s3Service.generatePresignedGetUrl("profile-images/", imageKey).toString();
        }

        return CommentDto.AuthorInfo.builder()
                .userId(user.getId())
                .nickname(user.getProfile().getNickname())
                .profileImageUrl(imageUrl)
                .build();
    }
}
