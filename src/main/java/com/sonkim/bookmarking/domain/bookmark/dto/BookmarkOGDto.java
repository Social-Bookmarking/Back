package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkOGDto {
    private String title;
    private String description;
    private String image;
}
