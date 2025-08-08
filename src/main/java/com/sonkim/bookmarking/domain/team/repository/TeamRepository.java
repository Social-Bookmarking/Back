package com.sonkim.bookmarking.domain.team.repository;

import com.sonkim.bookmarking.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> getByUser_Id(Long userId);
    Optional<Team> findByInviteCode(String inviteCode);
}
