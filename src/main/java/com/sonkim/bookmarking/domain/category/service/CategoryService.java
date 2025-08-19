package com.sonkim.bookmarking.domain.category.service;

import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.category.entity.Category;
import com.sonkim.bookmarking.domain.category.repository.CategoryRepository;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final BookmarkRepository bookmarkRepository;

    // 카테고리 생성
    @Transactional
    public void createCategory(Long userId, Long teamId, CategoryDto.CategoryRequestDto request) {
        log.info("userId: {}, teamId: {}, categoryName: {} 카테고리 생성 요청", userId, teamId, request.getName());

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 요청자가 권한이 있는지 검증
        teamMemberService.validateEditor(userId, teamId);

        // 동일한 이름의 카테고리가 있는지 확인
        if(categoryRepository.existsByNameAndTeam_Id(request.getName(), teamId)) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }

        // 카테고리 객체 생성 및 저장
        Team team = teamService.getTeamById(teamId);
        Category newCategory = Category.builder()
                .name(request.getName())
                .team(team)
                .build();
        categoryRepository.save(newCategory);
    }

    // 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponseDto> getCategoriesByTeam(Long teamId) {
        // 각 카테고리 정보와 북마크 개수 조회
        List<CategoryDto.CategoryResponseDto> categoriesWithCount = categoryRepository.findAllWithBookmarkCountByTeam_Id(teamId);

        // 그룹 전체 북마크 개수 조회
        long totalBookmarkCount = bookmarkRepository.countByTeam_Id(teamId);

        // '전체' 카테고리 생성하여 추가
        CategoryDto.CategoryResponseDto allCategories = CategoryDto.CategoryResponseDto.builder()
                .name("전체")
                .bookmarkCount(totalBookmarkCount)
                .build();

        List<CategoryDto.CategoryResponseDto> result = new ArrayList<>();
        result.add(allCategories);
        result.addAll(categoriesWithCount);

        return result;
    }

    // 카테고리 정보 수정
    @Transactional
    public void updateCategory(Long userId, Long categoryId, CategoryDto.CategoryRequestDto request) {
        log.info("userId: {}, categoryId: {} 카테고리 정보 수정 요청", userId, categoryId);

        // 카테고리 정보 가져오기
        Category category = getCategoryById(categoryId);

        // 그룹 상태 검증
        teamService.validateGroupIsActive(category.getTeam().getId());

        // 요청자가 권한이 있는지 검사
        teamMemberService.validateEditor(userId, category.getTeam().getId());

        // 카테고리 업데이트
        category.update(request);
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("userId: {}, categoryId: {} 카테고리 삭제 요청", userId, categoryId);

        // 카테고리 정보 가져오기
        Category category = getCategoryById(categoryId);

        // 그룹 상태 검증
        teamService.validateGroupIsActive(category.getTeam().getId());

        // 요청자가 권한이 있는지 검사
        teamMemberService.validateEditor(userId, category.getTeam().getId());

        // 해당 카테고리에 속해있던 북마크들을 '미분류' 상태로 분류
        bookmarkRepository.bulkSetCategoryToNull(categoryId);

        categoryRepository.delete(category);
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다."));
    }
}
