package com.sonkim.bookmarking.domain.category.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/groups/{groupId}/categories")
    public ResponseEntity<?> getCategories(@PathVariable("groupId") Long groupId) {
        List<CategoryDto.ResponseDto> categories = categoryService.getCategoriesByTeam(groupId);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/groups/{groupId}/categories")
    public ResponseEntity<?> createCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("groupId") Long groupId,
                                            @RequestBody CategoryDto.RequestDto request) {
        categoryService.createCategory(userDetails.getId(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("categoryId") Long categoryId,
                                            @RequestBody CategoryDto.RequestDto request) {
        categoryService.updateCategory(userDetails.getId(), categoryId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(userDetails.getId(), categoryId);
        return ResponseEntity.ok().build();
    }
}
