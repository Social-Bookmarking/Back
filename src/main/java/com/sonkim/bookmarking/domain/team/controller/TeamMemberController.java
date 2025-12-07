package com.sonkim.bookmarking.domain.team.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.team.dto.TeamMemberDto;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "그룹 멤버 관리", description = "그룹 멤버 조회, 역할 수정, 방출, 탈퇴 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @Operation(summary = "그룹 멤버 목록 조회", description = "특정 그룹에 속한 모든 멤버의 목록과 역할을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<TeamMemberDto.MemberResponseDto>> getMembers(@PathVariable("groupId") Long groupId) {
        List<TeamMemberDto.MemberResponseDto> members = teamMemberService.getTeamMembers(groupId);

        return ResponseEntity.ok(members);
    }

    @Operation(summary = "그룹 내 자신의 권한 조회", description = "로그인한 사용자의 특정 그룹에서의 권한을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 조회 성공"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}/permission")
    public ResponseEntity<TeamMemberDto.GetPermissionResponseDto> getMemberPermission(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                      @PathVariable("groupId") Long groupId) {
        Permission permission = teamMemberService.getUserPermissionInTeam(userDetails.getId(), groupId);
        return ResponseEntity.ok().body(TeamMemberDto.GetPermissionResponseDto.builder().permission(permission).build());
    }


    @Operation(summary = "멤버 역할 수정", description = "그룹 멤버의 역할을 변경합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 멤버를 찾을 수 없음")
    })
    @PatchMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> updateMemberPermission(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable("groupId") Long groupId,
                                                    @PathVariable("memberId") Long memberId,
                                                    @RequestBody TeamMemberDto.UpdatePermissionRequestDto dto) {
        teamMemberService.updateMemberPermission(userDetails.getId(), groupId, memberId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹에서 멤버 방출", description = "그룹에서 특정 멤버를 내보냅니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 방출 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신을 방출할 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 멤버를 찾을 수 없음")
    })
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> kickMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("groupId") Long groupId,
                                        @PathVariable("memberId") Long memberId) {
        teamMemberService.kickMember(userDetails.getId(), groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 탈퇴", description = "멤버 스스로 그룹에서 나갑니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 탈퇴 성공"),
            @ApiResponse(responseCode = "409", description = "마지막 남은 관리자는 탈퇴할 수 없음"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 멤버를 찾을 수 없음")
    })
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveTeam(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable("groupId") Long groupId) {
        teamMemberService.leaveTeam(userDetails.getId(), groupId);
        return ResponseEntity.noContent().build();
    }
}
