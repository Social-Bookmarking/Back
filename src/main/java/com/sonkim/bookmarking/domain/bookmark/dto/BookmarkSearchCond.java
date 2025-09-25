package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkSearchCond {
    private String keyword;
    private Long categoryId;
    private Boolean forMap;
    private Long tagId;
}
