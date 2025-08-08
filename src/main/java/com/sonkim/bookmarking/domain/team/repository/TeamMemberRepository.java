package com.sonkim.bookmarking.domain.team.repository;

import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.entity.TeamMember;
import com.sonkim.bookmarking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> getTeamMemberByUser_IdAndTeam_Id(Long userID, Long teamId);

    List<TeamMember> getByUser_Id(Long userId);

    boolean existsByUserAndTeam(User user, Team team);

    List<TeamMember> findAllByTeam_Id(Long teamId);
}
