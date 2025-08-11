package com.sonkim.bookmarking.domain.team.service;

import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.team.repository.TeamRepository;
import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.service.UserService;
import com.sonkim.bookmarking.common.exception.MemberAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TeamMemberService teamMemberService;

    @Transactional
    public void saveTeam(Team team) {
        teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("그룹 정보를 찾을 수 없습니다. teamId=" + teamId));
    }

    @Transactional(readOnly = true)
    public Team getTeamByInviteCode(String inviteCode) {
        return teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new EntityNotFoundException("그룹 정보를 찾을 수 없습니다. inviteCode=" + inviteCode));
    }

    @Transactional(readOnly = true)
    public String getInviteCodeByTeamId(Long teamId) {
        Team team = getTeamById(teamId);
        return team.getInviteCode();
    }

    // 그룹 상세정보 조회
    @Transactional(readOnly = true)
    public TeamDto.ResponseDto getTeamDetails(Long teamId) {
        Team team = getTeamById(teamId);

        return TeamDto.ResponseDto.builder()
                .name(team.getName())
                .description(team.getDescription())
                .ownerId(team.getUser().getId())
                .ownerName(team.getUser().getProfile().getNickname())
                .build();
    }

    // 사용자가 속한 그룹 목록 조회
    @Transactional(readOnly = true)
    public List<TeamDto.MyTeamDto> getMyTeams(Long userId) {
        // DB에서 사용자가 속한 모든 그룹 조회
        List<TeamMember> memberships = teamMemberService.getTeamsByUserId(userId);

        // 각 멤버십 정보에서 Team 정보를 추출하여 DTO로 반환
        return memberships.stream()
                .map(member -> {
                    Team team = member.getTeam();
                    return TeamDto.MyTeamDto.builder()
                            .teamId(team.getId())
                            .groupName(team.getName())
                            .description(team.getDescription())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 새로운 그룹 생성
    @Transactional
    public Team createTeam(Long userId, TeamDto.RequestDto createDto) {
        log.info("userId: {}, teamName: {} 생성 요청", userId, createDto.getName());

        // 그룹을 생성할 사용자 정보 가져오기
        User owner = userService.getUserById(userId);

        // 세로운 Team 객체 생성
        Team newTeam = Team.builder()
                .user(owner)
                .name(createDto.getName())
                .description(createDto.getDescription())
                .build();
        teamRepository.save(newTeam);

        // 초대 코드 생성
        String inviteCode = generateInviteCode(userId, newTeam.getId());
        newTeam.updateCode(inviteCode);

        // 생성자를 멤버로 추가
        TeamMember member = TeamMember.builder()
                .user(owner)
                .team(newTeam)
                .permission(Permission.ADMIN)
                .build();
        teamMemberService.save(member);

        return newTeam;
    }

    // 그룹 정보 수정
    @Transactional
    public void updateTeam(Long userId, Long teamId, TeamDto.RequestDto updateDto) {
        log.info("userId: {}, teamId: {} 정보 수정 요청", userId, teamId);

        // 요청자가 해당 그룹의 관리자인지 검증
        teamMemberService.validateAdmin(userId, teamId);

        // 그룹 정보 수정
        Team team = getTeamById(teamId);
        team.update(updateDto);
    }

    // 그룹 초대 코드 생성
    @Transactional
    public String generateInviteCode(Long userId, Long teamId) {
        log.info("userId: {}, teamId: {} 초대 코드 생성 요청", userId, teamId);

        // 요청자가 해당 그룹의 관리자인지 검증
        teamMemberService.validateAdmin(userId, teamId);

        // 그룹 정보 가져오기
        Team team = getTeamById(teamId);

        // 8자리의 영문-숫자 조합 랜덤 코드 생성
        RandomStringGenerator generator = RandomStringGenerator.builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .get();
        String newInviteCode = generator.generate(8);

        // 팀 정보에 새로운 초대 코드 업데이트
        team.updateCode(newInviteCode);

        return newInviteCode;
    }

    // 초대 코드를 통한 그룹 정보 조회
    @Transactional(readOnly = true)
    public TeamDto.ResponseDto getTeamPreviewByCode(String inviteCode) {
        Team team = getTeamByInviteCode(inviteCode);

        return TeamDto.ResponseDto.builder()
                .name(team.getName())
                .description(team.getDescription())
                .ownerId(team.getUser().getId())
                .ownerName(team.getUser().getProfile().getNickname())
                .build();
    }

    // 초대 코드를 통한 그룹 가입
    @Transactional
    public void joinTeam(Long userId, String inviteCode) {
        log.info("userId: {}, inviteCode: {} 그룹 가입 신청 요청", userId, inviteCode);

        // 초대 코드로 그룹 탐색
        Team team = getTeamByInviteCode(inviteCode);
        User user = userService.getUserById(userId);

        // 이미 해당 그룹의 멤버인지 확인
        if (teamMemberService.existsByUserAndTeam(user, team)) {
            throw new MemberAlreadyExistsException();
        }

        // 새로운 멤버로 추가
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .permission(Permission.VIEWER)
                .build();

        teamMemberService.save(teamMember);
    }
}
