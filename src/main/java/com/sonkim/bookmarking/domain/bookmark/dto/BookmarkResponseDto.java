package com.sonkim.bookmarking.domain.bookmark.dto;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkResponseDto {
    private Long bookmarkId;
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private boolean isLiked;
    private Long likesCount;
    private List<TagInfo> tags;
    private Long categoryId;

    @Data
    @AllArgsConstructor
    public static class TagInfo {
        private Long tagId;
        private String tagName;
    }

    public static BookmarkResponseDto from(Bookmark bookmark, boolean isLiked, Long likesCount, List<TagInfo> tags) {
        BookmarkResponseDto response = BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .url(bookmark.getUrl())
                .title(bookmark.getTitle())
                .description(bookmark.getDescription())
                .latitude(bookmark.getLatitude())
                .longitude(bookmark.getLongitude())
                .createdAt(bookmark.getCreatedAt())
                .isLiked(isLiked)
                .likesCount(likesCount)
                .tags(tags)
                .build();

        if (bookmark.getCategory() != null) {
            response.categoryId = bookmark.getCategory().getId();
        }

        return response;
    }
}
