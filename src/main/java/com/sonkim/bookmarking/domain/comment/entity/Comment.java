package com.sonkim.bookmarking.domain.comment.entity;

import com.sonkim.bookmarking.domain.comment.enums.CommentStatus;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 북마크 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;

    // 계정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    // 댓글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 부모 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    // 작성일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommentStatus status = CommentStatus.ACTIVE;

    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.content = "[삭제된 댓글입니다.]";
    }
}
