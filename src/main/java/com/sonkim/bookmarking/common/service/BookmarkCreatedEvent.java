package com.sonkim.bookmarking.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookmarkCreatedEvent {
    private Long bookmarkId;
    private String imageUrl;
}
