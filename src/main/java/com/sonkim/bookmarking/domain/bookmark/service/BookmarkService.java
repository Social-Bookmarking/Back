package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.account.service.AccountService;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkRequestDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import com.sonkim.bookmarking.domain.category.service.CategoryService;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CategoryService categoryService;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final AccountService accountService;

    // 북마크 등록
    @Transactional
    public Bookmark createBookmark(Long accountId, Long teamId, BookmarkRequestDto dto) {

        // 요청한 사용자의 그룹 내 역할 확인
        Permission userPermission = teamMemberService.getUserPermissionInTeam(accountId, teamId);

        // 권한이 VIEWER면 예외 발생
        if (userPermission == Permission.VIEWER) {
            throw new AuthorizationDeniedException("북마크를 생성할 권한이 없습니다.");
        }

        Account account = accountService.getAccountById(accountId);
        Team team = teamService.getTeamById(teamId);
        // Category category = categoryService.getCategoryById();

        Bookmark bookmark = Bookmark.builder()
                .account(account)
                .team(team)
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
                .orElseThrow(() -> new EntityNotFoundException("북마크를 찾을 수 없습니다. bookmarkId=" + bookmarkId));
    }

    // 북마크 정보 갱신
    @Transactional
    public void updateBookmark(Long accountId, Long bookmarkId, BookmarkRequestDto dto) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 요청한 사용자가 북마크를 작성한 사용자인지 확인
        if (!bookmark.getAccount().getId().equals(accountId)) {
            throw new AuthorizationDeniedException("해당 북마크를 수정할 권한이 없습니다.");
        }

        if(dto.getCategoryId() != null) {
            // Category category = categoryService.getCategoryById();
            bookmark.updateCategory(null);
        }

        bookmark.update(dto);
    }

    // 북마크 삭제
    @Transactional
    public void deleteBookmark(Long accountId, Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 요청한 사용자가 작성자인지 확인
        boolean isCreator = bookmark.getAccount().getId().equals(accountId);

        // 요청한 사용자가 그룹의 ADMIN인지 확인
        Permission userPermission = teamMemberService.getUserPermissionInTeam(accountId, bookmark.getTeam().getId());
        boolean isAdmin = userPermission == Permission.ADMIN;

        // 작성자도, ADMIN도 아니면 예외 발생
        if (!isCreator && !isAdmin) {
            throw new AuthorizationDeniedException("북마크를 삭제할 권한이 없습니다.");
        }

        bookmarkRepository.delete(bookmark);
    }

    // 그룹 내 모든 북마크 조회
    @Transactional(readOnly = true)
    public List<Bookmark> getBookmarksByTeamId(Long teamId) {
        return bookmarkRepository.getBookmarksByTeam_Id(teamId);
    }

    // 특정 카테고리에 속한 북마크 조회
    @Transactional(readOnly = true)
    public List<Bookmark> getBookmarksByTeamIdAndCategoryId(Long teamId, Long categoryId) {
        return bookmarkRepository.getBookmarksByTeam_IdAndCategory_Id(teamId, categoryId);
    }
}
