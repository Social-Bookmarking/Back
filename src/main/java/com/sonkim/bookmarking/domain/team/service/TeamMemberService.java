package com.sonkim.bookmarking.domain.team.service;

import com.sonkim.bookmarking.domain.team.dto.TeamMemberDto;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.repository.TeamMemberRepository;
import com.sonkim.bookmarking.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void save(TeamMember teamMember) {
        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public List<TeamMember> getTeamsByUserId(Long userId) {
        return teamMemberRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public Permission getUserPermissionInTeam(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.getTeamMemberByUser_IdAndTeam_Id(userId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        return member.getPermission();
    }

    @Transactional(readOnly = true)
    public void validateAdmin(Long userId, Long teamId) {
        Permission permission = getUserPermissionInTeam(userId, teamId);
        if (!permission.equals(Permission.ADMIN)) {
            throw new AuthorizationDeniedException("해당 명령을 수행할 권한이 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void validateEditor(Long userId, Long teamId) {
        Permission permission = getUserPermissionInTeam(userId, teamId);
        if (permission.equals(Permission.VIEWER)) {
            throw new AuthorizationDeniedException("해당 명령을 수행할 권한이 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Boolean existsByUserAndTeam(User user, Team team) {
        return teamMemberRepository.existsByUserAndTeam(user, team);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDto.MemberResponseDto> getTeamMembers(Long teamId) {
        List<TeamMember> members = teamMemberRepository.findAllByTeam_Id(teamId);

        return members.stream()
                .map(member -> TeamMemberDto.MemberResponseDto.builder()
                        .userId(member.getUser().getId())
                        .name(member.getUser().getProfile().getNickname())
                        .profileImageUrl(member.getUser().getProfile().getImageKey())
                        .permission(member.getPermission())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMemberPermission(Long userId, Long teamId, Long memberId, TeamMemberDto.UpdatePermissionRequestDto dto) {
        log.info("userId: {}, teamId: {}, memberId: {} 멤버 역할 수정 요청", userId, teamId, memberId);

        // 요청자가 ADMIN 권한인지 검증
        validateAdmin(userId, teamId);

        // 역할을 변경할 유저 정보 가져오기
        TeamMember member = teamMemberRepository.getTeamMemberByUser_IdAndTeam_Id(memberId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        // 역할 업데이트
        member.updatePermission(dto.getPermission());
    }

    @Transactional
    public void kickMember(Long userId, Long teamId, Long memberId) {
        log.info("userId: {}, teamId: {}, memberId: {} 멤버 방출 요청", userId, teamId, memberId);

        // 요청자가 ADMIN 권한인지 검증
        validateAdmin(userId, teamId);

        // 자기 자신 방출 방지
        if (userId.equals(memberId)) {
            throw new IllegalArgumentException("자기 자신은 방출할 수 없습니다.");
        }

        // 대상 유저 방출
        TeamMember memberToKick = teamMemberRepository.getTeamMemberByUser_IdAndTeam_Id(memberId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));
        teamMemberRepository.delete(memberToKick);
    }

    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        log.info("userId: {}, teamId: {}  그룹 탈퇴 요청", userId, teamId);

        // 탈퇴할 멤버 정보 가져오기
        TeamMember member = teamMemberRepository.getTeamMemberByUser_IdAndTeam_Id(userId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        // ADMIN은 무조건 한 명은 있도록 마지막 남은 ADMIN은 탈퇴 불가능
        if (member.getPermission().equals(Permission.ADMIN)) {
            long adminCount = teamMemberRepository.countByTeam_IdAndPermission(teamId, Permission.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("그룹의 관리자는 무조건 한 명 이상 존재해야 합니다.");
            }
        }

        // 멤버십 정보 삭제
        teamMemberRepository.delete(member);
    }
}
