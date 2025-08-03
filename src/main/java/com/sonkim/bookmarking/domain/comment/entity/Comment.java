package com.sonkim.bookmarking.domain.comment.entity;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private Account account;

    // 댓글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수정일
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
