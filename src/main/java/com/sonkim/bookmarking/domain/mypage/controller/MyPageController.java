package com.sonkim.bookmarking.domain.mypage.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.dto.PageResponseDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkResponseDto;
import com.sonkim.bookmarking.domain.mypage.dto.MyProfileDto;
import com.sonkim.bookmarking.domain.mypage.dto.PasswordDto;
import com.sonkim.bookmarking.domain.mypage.service.MyPageService;
import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이페이지", description = "내 정보 조회, 수정 등 마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MyPageController {

    private final MyPageService myPageService;
    private final TeamService teamService;

    @Operation(summary = "내 프로필 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/profile")
    public ResponseEntity<MyProfileDto.MyProfileRequestDto> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        MyProfileDto.MyProfileRequestDto myProfile = myPageService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(myProfile);
    }

    @Operation(summary = "내 프로필 정보 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 수정 성공")
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody MyProfileDto.UpdateNicknameRequestDto updateDto) {
        myPageService.updateNickname(userDetails.getId(), updateDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인 후, 새로운 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호가 일치하지 않음")
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestBody PasswordDto passwordDto) {
        myPageService.changePassword(userDetails.getId(), passwordDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 작성한 북마크 목록 조회",
            description = "현재 로그인한 사용자가 작성한 모든 북마크를 페이징하여 조회합니다.",
            parameters = {
                    @Parameter(name = "page", description = "표시할 페이지 (1부터 시작)")
            })
    @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    @GetMapping("/bookmarks")
    public ResponseEntity<PageResponseDto<BookmarkResponseDto>> getMyBookmarks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt") Pageable pageable) {
        PageResponseDto<BookmarkResponseDto> myBookmarks = myPageService.getMyBookmarks(userDetails.getId(), pageable);
        return ResponseEntity.ok(myBookmarks);
    }

    @Operation(summary = "내가 '좋아요'한 북마크 목록 조회",
            description = "현재 로그인한 사용자가 '좋아요'를 누른 북마크를 페이징하여 조회합니다.",
            parameters = {
                    @Parameter(name = "page", description = "표시할 페이지 (1부터 시작)")
            })
    @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    @GetMapping("/liked-bookmarks")
    public ResponseEntity<PageResponseDto<BookmarkResponseDto>> getMyLikedBookmarks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt") Pageable pageable) {

        PageResponseDto<BookmarkResponseDto> myLikedBookmarks = myPageService.getMyLikedBookmarks(userDetails.getId(), pageable);
        return ResponseEntity.ok(myLikedBookmarks);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 비활성화 처리하고 모든 세션을 종료시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        myPageService.deleteAccount(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내가 가입한 그룹 조회", description = "현재 로그인한 사용자가 속해있는 모든 그룹을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공")
    @GetMapping("/groups")
    public ResponseEntity<List<TeamDto.MyTeamDto>> getMyTeams(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TeamDto.MyTeamDto> myTeams = teamService.getMyTeams(userDetails.getId());
        return ResponseEntity.ok(myTeams);
    }
}
