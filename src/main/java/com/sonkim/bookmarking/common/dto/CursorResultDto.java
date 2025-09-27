package com.sonkim.bookmarking.common.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class CursorResultDto<T> {
    private final List<T> content;      // 데이터 목록
    private final Long nextCursor;      // 다음 페이지를 요청할 때 사용할 커서
    private final boolean hasNext;      // 다음 페이지 존재 여부

    public CursorResultDto(List<T> content, Long nextCursor, boolean hasNext) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
