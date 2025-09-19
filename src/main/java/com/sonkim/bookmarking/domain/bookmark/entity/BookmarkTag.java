package com.sonkim.bookmarking.domain.bookmark.entity;

import com.sonkim.bookmarking.domain.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookmark_tag")
public class BookmarkTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "bookmark_tag_seq_generator", sequenceName = "bookmark_tag_seq", allocationSize = 1)
    private Long id;

    // 북마크 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;

    // 태그 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
