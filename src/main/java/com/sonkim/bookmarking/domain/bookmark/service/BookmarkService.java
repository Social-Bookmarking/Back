package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.service.AccountService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkCreateDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import com.sonkim.bookmarking.domain.category.entity.Category;
import com.sonkim.bookmarking.domain.category.service.CategoryService;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CategoryService categoryService;
    private final TeamService teamService;
    private final AccountService accountService;

    // 북마크 등록
    @Transactional
    public Bookmark createBookmark(Long userId, BookmarkCreateDto dto) {
        Account account = accountService.getAccountById(userId);
        // Team team = teamService.getTeamById(dto.getTeamId());
        // Category category = categoryService.getCategoryById();

        Bookmark bookmark = Bookmark.builder()
                .account(account)
                .team(null)
                .category(null)
                .url(dto.getUrl())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        return bookmarkRepository.save(bookmark);
    }

    // 특정 북마크 조회
    @Transactional(readOnly = true)
    public Bookmark getBookmarkById(Long bookmarkId) {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new EntityNotFoundException("bookmarkId: " + bookmarkId + " not found"));
    }

    // 북마크 정보 갱신
    @Transactional
    public void updateBookmark(Long bookmarkId, BookmarkRequestDto dto) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        if(dto.getCategoryId() != null) {
            // Category category = categoryService.getCategoryById();
            bookmark.updateCategory(null);
        }

        bookmark.update(dto);
    }

    // 북마크 삭제
    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        bookmarkRepository.delete(bookmark);
    }
}
