package com.sonkim.bookmarking.domain.bookmark.entity;

import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkUpdateDto;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.category.entity.Category;
import com.sonkim.bookmarking.domain.team.entity.Team;
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
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

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

    // 임시 원본 이미지
    @Column(columnDefinition = "TEXT")
    private String originalImageUrl;

    // 최종 이미지 파일 키
    private String imageKey;

    private Double latitude;        // 위도
    private Double longitude;       // 경도

    // 생성일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수정일
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 북마크-태그 관계
    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<BookmarkTag> bookmarkTags = new ArrayList<>();

    // 북마크-좋아요 관계
    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<BookmarkLike> bookmarkLikes = new ArrayList<>();

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void update(BookmarkUpdateDto dto) {
        if(dto.getTitle() != null) this.title = dto.getTitle();
        if(dto.getDescription() != null) this.description = dto.getDescription();
        if(dto.getLatitude() != null) {
            if(dto.getLatitude() != -1)
                this.latitude = dto.getLatitude();
            else
                this.latitude = null;
        }
        if(dto.getLongitude() != null) {
            if (dto.getLongitude() != -1)
                this.longitude = dto.getLongitude();
            else
                this.longitude = null;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void updateOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }
}
