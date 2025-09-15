package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.common.service.BookmarkCreatedEvent;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.LikeCountDto;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    // 북마크 등록
    @Transactional
    public Bookmark createBookmark(Long userId, Long teamId, BookmarkRequestDto request) {
        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 요청한 사용자의 그룹 내 역할 확인
        Permission userPermission = teamMemberService.getUserPermissionInTeam(userId, teamId);

        // 권한이 VIEWER면 예외 발생
        if (userPermission == Permission.VIEWER) {
            throw new AuthorizationDeniedException("북마크를 생성할 권한이 없습니다.");
        }

        User user = userService.getUserById(userId);
        Team team = teamService.getTeamById(teamId);
        Category category = null;

        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryById(request.getCategoryId());
        }

        String imageKey = null;
        String originalImageUrl = null;

        if (request.getImageKey() != null && !request.getImageKey().isEmpty()) {
            // 사용자가 직접 이미지를 업로드한 경우
            // 파일을 temp -> bookmarks로 이동
            s3Service.moveFileToPermanentStorage("bookmarks/", request.getImageKey());
            imageKey = request.getImageKey();
        } else if (request.getOriginalImageUrl() != null && !request.getOriginalImageUrl().isEmpty()) {
            // OG 이미지 그대로 사용하는 경우
            // 임시 저장 후 비동기 처리 요청
            originalImageUrl = request.getOriginalImageUrl();
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .team(team)
                .category(category)
                .url(request.getUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .originalImageUrl(originalImageUrl)
                .imageKey(imageKey)
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

        // 비동기 작업 호출
        if (originalImageUrl != null) {
            eventPublisher.publishEvent(new BookmarkCreatedEvent(bookmark.getId(), originalImageUrl));
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
    public BookmarkResponseDto getBookmarkDetails(Long userID, Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 좋아요 수, 좋아요 여부, 태그 정보 조회
        Long likesCount = bookmarkLikeRepository.countBookmarkLikesByBookmark_Id(bookmarkId);
        boolean isLiked = bookmarkLikeRepository.existsBookmarkLikeByUser_IdAndBookmark_Id(userID, bookmarkId);
        List<BookmarkResponseDto.TagInfo> tags = bookmark.getBookmarkTags().stream()
                .map(bookmarkTag -> new BookmarkResponseDto.TagInfo(
                        bookmarkTag.getTag().getId(),
                        bookmarkTag.getTag().getName()
                ))
                .toList();

        // 데이터 조합
        BookmarkResponseDto response = BookmarkResponseDto.from(bookmark, isLiked, likesCount, tags);

        // 이미지 URL 생성
        String finalImageUrl = getFinalImageUrl(bookmark);
        response.setImageUrl(finalImageUrl);

        return response;
    }

    // 북마크 정보 갱신
    @Transactional
    public void updateBookmark(Long userId, Long bookmarkId, BookmarkRequestDto dto) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 그룹 상태 검증
        teamService.validateGroupIsActive(bookmark.getTeam().getId());

        // 요청한 사용자가 북마크를 작성한 사용자인지 확인
        if (!bookmark.getUser().getId().equals(userId)) {
            throw new AuthorizationDeniedException("해당 북마크를 수정할 권한이 없습니다.");
        }

        // 카테고리 업데이트
        if(dto.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(dto.getCategoryId());
            bookmark.updateCategory(category);
        }

        // 이미지 업데이트
        String oldImageKey = bookmark.getImageKey();

        if (dto.getImageKey() != null) {
            if (dto.getImageKey().isEmpty()) {
                // 이미지를 삭제하려는 경우
                bookmark.updateImageKey(null);
                if (oldImageKey != null) {
                    s3Service.deleteFile("bookmarks/", oldImageKey);
                }
            } else {
                // 이미지를 변경하려는 경우
                s3Service.moveFileToPermanentStorage("bookmarks/", dto.getImageKey());
                bookmark.updateImageKey(dto.getImageKey());
                if (oldImageKey != null) {
                    s3Service.deleteFile("bookmarks/", oldImageKey);
                }

                // 기존 OG 이미지는 삭제
                if (bookmark.getOriginalImageUrl() != null) {
                    bookmark.updateOriginalImageUrl(null);
                }
            }
        }

        bookmark.update(dto);
    }

    // 북마크 삭제
    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);

        // 그룹 상태 검증
        teamService.validateGroupIsActive(bookmark.getTeam().getId());

        // 요청한 사용자가 작성자인지 확인
        boolean isCreator = bookmark.getUser().getId().equals(userId);

        // 작성자도, ADMIN도 아니면 예외 발생
        if (!isCreator && !teamMemberService.validateAdmin(userId, bookmark.getTeam().getId())) {
            throw new AuthorizationDeniedException("북마크를 삭제할 권한이 없습니다.");
        }

        bookmarkRepository.delete(bookmark);
    }

    // 그룹 내 모든 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTeamId(Long userId, Long teamId, Pageable pageable) {
        // 북마크 페이징 조회
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_Id(teamId, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 특정 카테고리에 속한 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTeamIdAndCategoryId(Long userId, Long teamId, Long categoryId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndCategory_Id(teamId, categoryId, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 북마크 검색
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> searchBookmarksByTeamId(Long userId, Long teamId, String keyword, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndKeyword(teamId, keyword, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 북마크 검색(특정 카테고리 내)
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> searchBookmarksByTeamIdAndCategoryId(Long userId, Long teamId, Long categoryId, String keyword, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByTeam_IdAndCategory_IdAndKeyword(teamId, categoryId, keyword, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 북마크 태그 검색
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTagInGroup(Long userId, Long teamId, Long tagId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByTeam_IdAndTag_Id(teamId, tagId, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 북마크 태그 검색(특정 카테고리 내)
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksByTagInCategory(Long userId, Long teamId, Long categoryId, Long tagId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByCategory_IdAndTag_Id(teamId, categoryId, tagId, pageable);
        return enrichBookmarksWithDetails(bookmarks, userId);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getBookmarksForMap(Long teamId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByTeam_IdAndLatitudeIsNotNullAndLongitudeIsNotNull(teamId, pageable);
        return enrichBookmarksWithDetails(bookmarks, teamId);
    }

    // 추가 정보 채우기
    public PageResponseDto<BookmarkResponseDto> enrichBookmarksWithDetails(Page<Bookmark> bookmarks, Long userId) {
        List<Long> bookmarkIds = bookmarks.getContent().stream().map(Bookmark::getId).toList();

        // 좋아요 개수, 좋아요 여부, 태그 정보 전부 다 가져오기
        Map<Long, Long> likesCountMap = bookmarkLikeRepository.findLikesCountForBookmarks(bookmarkIds)
                .stream().collect(Collectors.toMap(LikeCountDto::getBookmarkId, LikeCountDto::getCount));
        List<Long> likedBookmarkIds = bookmarkLikeRepository.findLikedBookmarkIdsForUser(userId, bookmarkIds);
        List<BookmarkTag> allBookmarkTags = bookmarkTagRepository.findAllByBookmarkIdsWithTags(bookmarkIds);
        Map<Long, List<BookmarkResponseDto.TagInfo>> tagsMap = allBookmarkTags.stream()
                .collect(Collectors.groupingBy(
                        bt -> bt.getBookmark().getId(),
                        Collectors.mapping(
                                bt -> new BookmarkResponseDto.TagInfo(bt.getTag().getId(), bt.getTag().getName()),
                                Collectors.toList()
                        )
                ));

        // 데이터 조합
        Page<BookmarkResponseDto> dtoPage = bookmarks.map(bookmark -> {
            long likesCount = likesCountMap.getOrDefault(bookmark.getId(), 0L);
            boolean isLiked = likedBookmarkIds.contains(bookmark.getId());
            List<BookmarkResponseDto.TagInfo> tags = tagsMap.getOrDefault(bookmark.getId(), Collections.emptyList());

            BookmarkResponseDto dto = BookmarkResponseDto.from(bookmark, isLiked, likesCount, tags);

            String finalImageUrl = getFinalImageUrl(bookmark);
            dto.setImageUrl(finalImageUrl);
            return dto;
        });

        return new PageResponseDto<>(dtoPage);
    }

    private String getFinalImageUrl(Bookmark bookmark) {
        if (bookmark.getImageKey() != null) {
            return s3Service.generatePresignedGetUrl("bookmarks/", bookmark.getImageKey()).toString();
        } else if (bookmark.getOriginalImageUrl() != null) {
            return bookmark.getOriginalImageUrl();
        }
        return null;
    }
}
