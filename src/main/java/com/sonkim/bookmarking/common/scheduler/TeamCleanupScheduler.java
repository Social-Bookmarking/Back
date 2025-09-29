package com.sonkim.bookmarking.common.scheduler;

import com.sonkim.bookmarking.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamCleanupScheduler {
    private final TeamService teamService;

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteScheduledTeams() {
        log.info("삭제가 예정된 그룹 정리 작업 시작...");
        teamService.deletePendingTeams();
        log.info("삭제가 예정된 그룹 저일 작업 완료.");
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void cleanupOrphanedTeams() {
        log.info("주인 없는 그룹 정리 작업 시작...");
        teamService.deleteOrphanedTeams();
        log.info("주인 없는 그룹 정리 작업 완료.");
    }
}
