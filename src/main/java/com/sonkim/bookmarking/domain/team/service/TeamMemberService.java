package com.sonkim.bookmarking.domain.team.service;

import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.repository.TeamMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void save(TeamMember teamMember) {
        teamMemberRepository.save(teamMember);
    }

    @Transactional(readOnly = true)
    public Permission getUserPermissionInTeam(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.getTeamMemberByUser_IdAndTeam_Id(userId, teamId);
        if (member == null) {
            throw new EntityNotFoundException("해당 유저는 그룹에 속해 있지 않습니다.");
        }

        return member.getPermission();
    }
}
