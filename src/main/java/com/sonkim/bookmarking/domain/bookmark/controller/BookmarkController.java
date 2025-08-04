package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkTestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @ResponseBody
    @GetMapping("/test")
    public BookmarkTestDto getNaverMapBookmarkTestDto(@RequestParam String url) {
        try {
            return bookmarkService.getOpenGraphData(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new BookmarkTestDto();
        }
    }
}
