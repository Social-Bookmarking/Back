package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;

public interface OgExtractorStrategy {
    boolean supports(String url);   // 전달된 url을 처리할 수 있는 전략인지 확인
    BookmarkOGDto extract(String url);   // HTML 추출
}
