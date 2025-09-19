package com.sonkim.bookmarking.domain.tag.entity;

import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkTag;
import com.sonkim.bookmarking.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "tag_seq_generator", sequenceName = "tag_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "tag")
    @Builder.Default
    @ToString.Exclude
    private List<BookmarkTag> bookmarkTags = new ArrayList<>();
}
