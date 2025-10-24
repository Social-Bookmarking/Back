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
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<CategoryDto.CategoryResponseDto> createCategory(Long userId, Long teamId, CategoryDto.CategoryRequestDto request) {
        log.info("userId: {}, teamId: {}, categoryName: {} 카테고리 생성 요청", userId, teamId, request.getName());

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 요청자가 권한이 있는지 검증
        teamMemberService.validateEditor(userId, teamId);

        // 동일한 이름의 카테고리가 있는지 확인
        if(categoryRepository.existsByNameAndTeam_Id(request.getName(), teamId)) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }

        // 현재 그룹의 카테고리 개수
        long currentCategoryCount = categoryRepository.countByTeam_Id(teamId);

        // 카테고리 객체 생성 및 저장
        Team team = teamService.getTeamById(teamId);
        Category newCategory = Category.builder()
                .name(request.getName())
                .team(team)
                .position((int) currentCategoryCount + 1)
                .build();
        categoryRepository.save(newCategory);

        return getCategoriesByTeam(teamId);
    }

    // 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponseDto> getCategoriesByTeam(Long teamId) {
        // 각 카테고리 정보와 북마크 개수 조회
        List<CategoryDto.CategoryResponseDto> categoriesWithCount = categoryRepository.findAllWithBookmarkCountByTeamId(teamId);

        // 그룹 전체 북마크 개수 조회
        long totalBookmarkCount = bookmarkRepository.countByTeam_Id(teamId);

        // '전체' 카테고리 생성하여 추가
        CategoryDto.CategoryResponseDto allCategories = CategoryDto.CategoryResponseDto.builder()
                .id(-1L)
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
    public List<CategoryDto.CategoryResponseDto> updateCategory(Long userId, Long categoryId, CategoryDto.CategoryRequestDto request) {
        log.info("userId: {}, categoryId: {} 카테고리 정보 수정 요청", userId, categoryId);

        // 카테고리 정보 가져오기
        Category category = getCategoryById(categoryId);
        Long teamId = category.getTeam().getId();

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 요청자가 권한이 있는지 검사
        teamMemberService.validateEditor(userId, teamId);

        // 동일한 이름의 카테고리가 있는지 확인
        if(categoryRepository.existsByNameAndTeam_Id(request.getName(), teamId)) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }

        // 카테고리 업데이트
        category.update(request);

        return getCategoriesByTeam(category.getTeam().getId());
    }

    // 카테고리 삭제
    @Transactional
    public List<CategoryDto.CategoryResponseDto> deleteCategory(Long userId, Long categoryId) {
        log.info("userId: {}, categoryId: {} 카테고리 삭제 요청", userId, categoryId);

        // 카테고리 정보 가져오기
        Category category = getCategoryById(categoryId);
        Long teamId = category.getTeam().getId();

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 요청자가 권한이 있는지 검사
        teamMemberService.validateEditor(userId, teamId);

        // 해당 카테고리에 속해있던 북마크들을 '미분류' 상태로 분류
        bookmarkRepository.bulkSetCategoryToNull(categoryId);

        // 카테고리 삭제
        categoryRepository.delete(category);

        return getCategoriesByTeam(teamId);
    }

    // 카테고리 순서 수정
    @Transactional
    public void updateCategoryPositions(Long userId, Long teamId, List<CategoryDto.UpdatePositionRequestDto> requests) {
        log.info("userId: {}, teamId: {} 카테고리 순서 수정 요청", userId, teamId);

        // 요청자가 EDITOR 권한이 있는지 검사
        teamMemberService.validateEditor(userId, teamId);

        // 요청된 모든 카테고리 ID 조회
        List<Long> categoryIds = requests.stream().map(CategoryDto.UpdatePositionRequestDto::getCategoryId).toList();
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        // 각 카테고리의 position 값 업데이트
        for (CategoryDto.UpdatePositionRequestDto request : requests) {
            Category category = categoryMap.get(request.getCategoryId());
            category.updatePosition(request.getPosition());
        }
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다."));
    }
}
