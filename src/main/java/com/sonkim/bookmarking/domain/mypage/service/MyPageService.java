package com.sonkim.bookmarking.domain.mypage.service;

import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import com.sonkim.bookmarking.domain.bookmark.service.BookmarkService;
import com.sonkim.bookmarking.domain.mypage.dto.MyProfileDto;
import com.sonkim.bookmarking.domain.mypage.dto.PasswordDto;
import com.sonkim.bookmarking.auth.token.service.TokenService;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.repository.UserRepository;
import com.sonkim.bookmarking.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkService bookmarkService;
    private final UserService userService;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public MyProfileDto.MyProfileResponseDto getMyProfile(Long userId) {
        // 프로필 정보 조회
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다. userId=" + userId));
        String imageUrl = null;
        String imageKey = user.getProfile().getImageKey();

        if (imageKey != null) {
            imageUrl = s3Service.generatePresignedGetUrl("profile-images/", imageKey).toString();
        }

        // DTO로 변환하여 반환
        return MyProfileDto.MyProfileResponseDto.builder()
                .nickname(user.getProfile().getNickname())
                .profileImageUrl(imageUrl)
                .build();
    }

    // 프로필 업데이트
    @Transactional
    public void updateProfile(Long userId, MyProfileDto.UpdateRequestDto updateRequestDto) {
        log.info("userId: {} 프로필 업데이트 요청", userId);

        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다. userId=" + userId));

        String imageKey = updateRequestDto.getImageKey();

        if (imageKey != null && !imageKey.isBlank()) {
            s3Service.moveFileToPermanentStorage("profile-images/", imageKey);
        }

        user.getProfile().update(updateRequestDto.getNickname(), updateRequestDto.getImageKey());
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, PasswordDto passwordDto) {
        log.info("userId: {} 비밀번호 변경 요청", userId);

        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다. userId=" + userId));

        // 현재 비밀번호 일치하는지 확인
        if(!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호로 변경
        user.updatePassword(passwordEncoder.encode(passwordDto.getNewPassword()));

        // refresh token 삭제
        tokenService.deleteRefreshToken(userId);
    }

    // 사용자가 작성한 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getMyBookmarks(Long userId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByUser_Id(userId, pageable);

        return bookmarkService.enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 사용자가 좋아요를 누른 북마크 조회
    @Transactional(readOnly = true)
    public PageResponseDto<BookmarkResponseDto> getMyLikedBookmarks(Long userId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findLikedBookmarksByUser_Id(userId, pageable);

        return bookmarkService.enrichBookmarksWithDetails(bookmarks, userId);
    }

    // 탈퇴 처리
    public void deleteAccount(Long userId) {
        log.info("userId: {} 탈퇴 요청", userId);

        // 사용자 정보 불러와서 탈퇴 처리
        User user = userService.getUserById(userId);
        user.withdraw();

        // RefreshToken 삭제하여 세션 무효화
        tokenService.deleteRefreshToken(userId);

        // 마지막 관리자인 그룹이 있는 경우 탈퇴가 불가능하도록 로직 추가 필요
    }
}
