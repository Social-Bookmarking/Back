package com.sonkim.bookmarking.domain.bookmark.entity;

import com.sonkim.bookmarking.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookmark_like")
public class BookmarkLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    // 북마크 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;
}
