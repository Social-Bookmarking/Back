package com.sonkim.bookmarking.domain.team.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.team.dto.TeamMemberDto;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/groups")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    // 그룹 멤버 조회
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getMembers(@PathVariable("groupId") Long groupId) {
        List<TeamMemberDto.MemberResponseDto> members = teamMemberService.getTeamMembers(groupId);

        return ResponseEntity.ok(members);
    }

    // 멤버 역할 수정
    @PatchMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> updateMemberPermission(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable("groupId") Long groupId,
                                                    @PathVariable("memberId") Long memberId,
                                                    @RequestBody TeamMemberDto.UpdatePermissionRequestDto dto) {
        teamMemberService.updateMemberPermission(userDetails.getId(), groupId, memberId, dto);
        return ResponseEntity.ok().build();
    }

    // 멤버 방출
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> kickMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("groupId") Long groupId,
                                        @PathVariable("memberId") Long memberId) {
        teamMemberService.kickMember(userDetails.getId(), groupId, memberId);
        return ResponseEntity.ok().build();
    }
}
