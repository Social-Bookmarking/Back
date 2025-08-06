package com.sonkim.bookmarking.domain.bookmark.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BookmarkCreateDto extends BookmarkRequestDto {
    private Long teamId;
}
