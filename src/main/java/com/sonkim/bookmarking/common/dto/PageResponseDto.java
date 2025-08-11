package com.sonkim.bookmarking.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponseDto<T> {

    private final List<T> content;          // 데이터 목록
    private final int pageNumber;         // 현재 페이지 번호 (0-based)
    private final int pageSize;           // 페이지 크기
    private final int totalPages;         // 전체 페이지 수
    private final long totalElements;     // 전체 요소 수
    private final boolean isFirst;        // 첫 페이지 여부
    private final boolean isLast;         // 마지막 페이지 여부

    public PageResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
    }
}
