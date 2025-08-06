package com.sonkim.bookmarking.domain.tag.controller;

import com.sonkim.bookmarking.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/tag")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<?> getTags() {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createTag() {
        return null;
    }

    @PatchMapping
    public ResponseEntity<?> updateTag() {
        return null;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTag() {
        return null;
    }
}
