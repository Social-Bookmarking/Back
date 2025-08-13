package com.sonkim.bookmarking.common.scheduler;

import com.sonkim.bookmarking.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamCleanupScheduler {
    private final TeamService teamService;

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteScheduledTeams() {
        teamService.deletePendingTeams();
    }
}
