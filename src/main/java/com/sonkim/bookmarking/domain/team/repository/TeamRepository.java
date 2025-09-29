package com.sonkim.bookmarking.domain.team.repository;

import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByInviteCode(String inviteCode);

    List<Team> findAllByStatusAndDeletionScheduledAtBefore(TeamStatus status, LocalDateTime deletionScheduledAtBefore);

    @Query("SELECT t FROM Team t WHERE NOT EXISTS " +
            "(SELECT 1 FROM TeamMember tm JOIN tm.user u " +
            "WHERE tm.team = t AND u.userStatus = 'ACTIVE')")
    List<Team> findOrphanedTeams();
}
