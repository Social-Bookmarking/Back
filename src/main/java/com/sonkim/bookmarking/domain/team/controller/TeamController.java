package com.sonkim.bookmarking.domain.team.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/groups")
public class TeamController {

    private final TeamService teamService;

    // 새로운 그룹 생성
    @PostMapping
    public ResponseEntity<?> createTeam(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TeamDto.RequestDto createDto) {
        Team newTeam = teamService.createTeam(userDetails.getId(), createDto);

        Map<String, Long> response = new HashMap<>();
        response.put("groupId", newTeam.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 그룹 상세 정보 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getTeamDetails(@PathVariable Long groupId) {
        TeamDto.ResponseDto responseDto = teamService.getTeamDetails(groupId);
        return ResponseEntity.ok(responseDto);
    }

    // 그룹 상세 정보 업데이트
    @PatchMapping("/{groupId}")
    public ResponseEntity<?> updateTeam(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TeamDto.RequestDto updateDto,
                                        @PathVariable("groupId") Long groupId) {
        teamService.updateTeam(userDetails.getId(), groupId, updateDto);
        return ResponseEntity.ok().build();
    }

    // 초대코드 생성 요청
    @PostMapping("/{groupId}/invite-code")
    public ResponseEntity<?> updateInviteCode(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable("groupId") Long groupId) {
        String newInviteCode = teamService.generateInviteCode(userDetails.getId(), groupId);

        Map<String, String> response = new HashMap<>();
        response.put("inviteCode", newInviteCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 초대코드를 통한 그룹 정보 조회
    @GetMapping("/join")
    public ResponseEntity<?> getTeamDetailsByCode(@RequestParam String code) {
        TeamDto.ResponseDto responseDto = teamService.getTeamPreviewByCode(code);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinTeamMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody TeamDto.JoinRequestDto joinRequestDto) {
        teamService.joinTeam(userDetails.getId(), joinRequestDto.getInviteCode());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
