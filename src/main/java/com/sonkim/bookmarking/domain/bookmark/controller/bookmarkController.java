package com.sonkim.bookmarking.domain.bookmark.controller;

import com.sonkim.bookmarking.domain.bookmark.service.bookmarkService;
import com.sonkim.bookmarking.domain.bookmark.dto.bookmarkTestDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping("/api/bookmark")
public class bookmarkController {

    private final bookmarkService bookmarkService;

    public bookmarkController(bookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @ResponseBody
    @GetMapping("/test")
    public bookmarkTestDto getNaverMapBookmarkTestDto(@RequestParam String url) {
        try {
            return bookmarkService.getOpenGraphData(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new bookmarkTestDto();
        }
    }
}
