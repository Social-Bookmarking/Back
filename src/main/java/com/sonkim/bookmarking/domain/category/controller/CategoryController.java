package com.sonkim.bookmarking.domain.category.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리 관리", description = "그룹 내 카테고리 생성, 조회, 수정, 삭제 관련 API")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "그룹의 모든 카테고리 목록 조회", description = "특정 그룹에 속한 모든 카테고리의 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    @GetMapping("/groups/{groupId}/categories")
    public ResponseEntity<List<CategoryDto.CategoryResponseDto>> getCategories(@PathVariable("groupId") Long groupId) {
        List<CategoryDto.CategoryResponseDto> categories = categoryService.getCategoriesByTeam(groupId);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "새로운 카테고리 생성", description = "그룹 내에 새로운 카테고리를 생성합니다. EDITOR 이상의 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (VIEWER는 생성 불가)"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리 이름")
    })
    @PostMapping("/groups/{groupId}/categories")
    public ResponseEntity<List<CategoryDto.CategoryResponseDto>> createCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("groupId") Long groupId,
                                            @RequestBody @Valid CategoryDto.CategoryRequestDto request) {
        List<CategoryDto.CategoryResponseDto> updatedCategories = categoryService.createCategory(userDetails.getId(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCategories);
    }

    @Operation(summary = "카테고리 정보 수정", description = "특정 카테고리의 이름을 수정합니다. EDITOR 이상의 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<List<CategoryDto.CategoryResponseDto>> updateCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("categoryId") Long categoryId,
                                            @RequestBody @Valid CategoryDto.CategoryRequestDto request) {
        List<CategoryDto.CategoryResponseDto> updatedCategories = categoryService.updateCategory(userDetails.getId(), categoryId, request);
        return ResponseEntity.ok(updatedCategories);
    }

    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 삭제합니다. EDITOR 이상의 권한이 필요합니다. 삭제 시 해당 카테고리에 속한 북마크들은 '미분류' 상태가 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<List<CategoryDto.CategoryResponseDto>> deleteCategory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("categoryId") Long categoryId) {
        List<CategoryDto.CategoryResponseDto> updatedCategories = categoryService.deleteCategory(userDetails.getId(), categoryId);
        return ResponseEntity.ok(updatedCategories);
    }

    @Operation(summary = "카테고리 순서 일괄 수정", description = "특정 그룹 내 카테고리들의 표시 순서를 한 번에 업데이트합니다. EDITOR 이상의 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "순서 업데이트 성공")
    @PatchMapping("/groups/{groupId}/categories/order")
    public ResponseEntity<Void> updateCategoryPositions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody @Valid CategoryDto.CategoryOrderUpdateRequest requests
    ) {
        categoryService.updateCategoryPositions(userDetails.getId(), groupId, requests.getCategories());
        return ResponseEntity.ok().build();
    }
}
