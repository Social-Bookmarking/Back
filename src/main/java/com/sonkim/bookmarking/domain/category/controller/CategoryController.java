package com.sonkim.bookmarking.domain.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/category")
public class CategoryController {

    @GetMapping
    public ResponseEntity<?> getCategories() {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createCategory() {
        return null;
    }

    @PatchMapping
    public ResponseEntity<?> updateCategory() {
        return null;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory() {
        return null;
    }
}
