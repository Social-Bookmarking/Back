package com.sonkim.bookmarking.mypage.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MyPageController {

    private final TeamService teamService;

    // 사용자가 속한 그룹 목록 조회
    @GetMapping("/groups")
    public ResponseEntity<?> getMyTeams(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TeamDto.MyTeamDto> myTeams = teamService.getMyTeams(userDetails.getId());
        return ResponseEntity.ok(myTeams);
    }
}
