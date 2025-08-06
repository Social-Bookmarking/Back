package com.sonkim.bookmarking.domain.bookmark.entity;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.category.entity.Category;
import com.sonkim.bookmarking.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 그룹 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // 카테고리 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 원본 링크
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    // 제목
    @Column(length = 100)
    private String title;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 썸네일
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private Double latitude;        // 위도
    private Double longitude;       // 경도

    // 생성일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수정일
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void update(BookmarkRequestDto dto) {
        if(dto.getTitle() != null) this.title = dto.getTitle();
        if(dto.getDescription() != null) this.description = dto.getDescription();
        if(dto.getImageUrl() != null) this.imageUrl = dto.getImageUrl();
        if(dto.getLatitude() != null) this.latitude = dto.getLatitude();
        if(dto.getLongitude() != null) this.longitude = dto.getLongitude();
        this.updatedAt = LocalDateTime.now();
    }
}
