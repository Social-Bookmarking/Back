package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.BookmarkTag;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkLikeRepository;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkTagRepository;
import com.sonkim.bookmarking.domain.category.entity.Category;
import com.sonkim.bookmarking.domain.tag.entity.Tag;
import com.sonkim.bookmarking.domain.tag.repository.TagRepository;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkLikeRepository bookmarkLikeRepository;
    private final TagRepository tagRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final CategoryService categoryService;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final UserService userService;

    // 북마크 등록
    @Transactional
    public Bookmark createBookmark(Long userId, Long teamId, BookmarkRequestDto request) {

        // 요청한 사용자의 그룹 내 역할 확인
        Permission userPermission = teamMemberService.getUserPermissionInTeam(userId, teamId);

        // 권한이 VIEWER면 예외 발생
        if (userPermission == Permission.VIEWER) {
            throw new AuthorizationDeniedException("북마크를 생성할 권한이 없습니다.");
        }

        User user = userService.getUserById(userId);
        Team team = teamService.getTeamById(teamId);
        Category category = categoryService.getCategoryById(request.getCategoryId());

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .team(team)
                .category(category)
                .url(request.getUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        bookmarkRepository.save(bookmark);

        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            // ID 리스트로 Tag 엔티티들 조회
            List<Tag> tags = tagRepository.findAllById(tagIds);

            // 각 Tag에 대해 BookmarkTag 엔티티 생성
            for (Tag tag : tags) {
                BookmarkTag bookmarkTag = BookmarkTag.builder()
                        .bookmark(bookmark)
                        .tag(tag)
                        .build();
                bookmarkTagRepository.save(bookmarkTag);
            }
        }

        return bookmark;
    }

    // 특정 북마크 조회
    @Transactional(readOnly = true)
    public Bookmark getBookmarkById(Long bookmarkId) {
        return bookmarkRepository.findByIdWithTags(bookmarkId)
                .orElseThrow(() -> new EntityNotFoundException("북마크를 찾을 수 없습니다. bookmarkId=" + bookmarkId));
    }

    @Transactional(readOnly = true)
    public BookmarkResponseDto getBookmarkDtoById(Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        Long likesCount = bookmarkLikeRepository.countBookmarkLikesByBookmark_Id(bookmarkId);
        return BookmarkResponseDto.fromEntityWithLikes(bookmark, likesCount);
    }

    // 북마크 정보 갱신
    @Transactional
    public void updateBookmark(Long userId, Long bookmarkId, BookmarkRequestDto dto) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 요청한 사용자가 북마크를 작성한 사용자인지 확인
        if (!bookmark.getUser().getId().equals(userId)) {
            throw new AuthorizationDeniedException("해당 북마크를 수정할 권한이 없습니다.");
        }

        if(dto.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(dto.getCategoryId());
            bookmark.updateCategory(category);
        }

        bookmark.update(dto);
    }

    // 북마크 삭제
    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 요청한 사용자가 작성자인지 확인
        boolean isCreator = bookmark.getUser().getId().equals(userId);

        // 요청한 사용자가 그룹의 ADMIN인지 확인
        Permission userPermission = teamMemberService.getUserPermissionInTeam(userId, bookmark.getTeam().getId());
        boolean isAdmin = userPermission == Permission.ADMIN;

        // 작성자도, ADMIN도 아니면 예외 발생
        if (!isCreator && !isAdmin) {
            throw new AuthorizationDeniedException("북마크를 삭제할 권한이 없습니다.");
        }

        bookmarkRepository.delete(bookmark);
    }

    // 그룹 내 모든 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTeamId(Long teamId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_Id(teamId, pageable);

        // BookmarkDto로 변환하여 전달
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));

        return new PageResponseDto<>(dtoPage);
    }

    // 특정 카테고리에 속한 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTeamIdAndCategoryId(Long teamId, Long categoryId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndCategory_Id(teamId, categoryId, pageable);

        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));

        return new PageResponseDto<>(dtoPage);
    }

    // 북마크 검색
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> searchBookmarksByTeamId(Long teamId, String keyword, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndKeyword(teamId, keyword, pageable);
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));
        return new PageResponseDto<>(dtoPage);
    }

    // 북마크 검색(특정 카테고리 내)
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> searchBookmarksByTeamIdAndCategoryId(Long teamId, Long categoryId, String keyword, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndCategory_IdAndKeyword(teamId, categoryId, keyword, pageable);
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));
        return new PageResponseDto<>(dtoPage);
    }

    // 북마크 태그 검색
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTagInGroup(Long teamId, Long tagId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByTeam_IdAndTag_Id(teamId, tagId, pageable);
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));
        return new PageResponseDto<>(dtoPage);
    }

    // 북마크 태그 검색(특정 카테고리 내)
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTagInCategory(Long teamId, Long categoryId, Long tagId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByCategory_IdAndTag_Id(teamId, categoryId, tagId, pageable);
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> getBookmarkDtoById(bookmark.getId()));
        return new PageResponseDto<>(dtoPage);
    }

}
