package com.sonkim.bookmarking.domain.team.repository;

import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.team.enums.Permission;
import com.sonkim.bookmarking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> getTeamMemberByUser_IdAndTeam_Id(Long userID, Long teamId);

    List<TeamMember> findAllByUser_Id(Long userId);

    boolean existsByUserAndTeam(User user, Team team);

    List<TeamMember> findAllByTeam_Id(Long teamId);

    long countByTeam_IdAndPermission(Long teamId, Permission permission);

    long countByTeam_Id(Long teamId);

    Optional<TeamMember> findByUser_IdAndTeam_Id(Long userId, Long teamId);

    Long user(User user);
}
