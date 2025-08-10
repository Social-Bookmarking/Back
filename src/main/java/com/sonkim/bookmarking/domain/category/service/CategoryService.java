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

import java.util.List;
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
    public void createCategory(Long userId, Long teamId, CategoryDto.RequestDto request) {
        log.info("userId: {}, teamId: {}, categoryName: {} 카테고리 생성 요청", userId, teamId, request.getName());

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
                .description(request.getDescription())
                .team(team)
                .build();
        categoryRepository.save(newCategory);
    }

    // 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryDto.ResponseDto> getCategoriesByTeam(Long teamId) {
        List<Category> categories = categoryRepository.findAllByTeam_Id(teamId);
        return categories.stream()
                .map(category -> CategoryDto.ResponseDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // 카테고리 정보 수정
    @Transactional
    public void updateCategory(Long userId, Long categoryId, CategoryDto.RequestDto request) {
        log.info("userId: {}, categoryId: {} 카테고리 정보 수정 요청", userId, categoryId);

        // 카테고리 정보 가져오기
        Category category = getCategoryById(categoryId);

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
