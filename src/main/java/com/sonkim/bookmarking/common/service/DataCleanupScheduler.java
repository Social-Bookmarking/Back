package com.sonkim.bookmarking.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataCleanupScheduler {

    private final DatabaseCleanupService cleanupService;

    @Scheduled(cron = "0 0 4 * * *")
    public void scheduleDatabaseCleanup() {
        cleanupService.runCleanup("CLEANUP_UNUSED_TAGS");
        cleanupService.runCleanup("CLEANUP_PENDING_TEAMS");
        cleanupService.runCleanup("CLEANUP_ORPHANED_TEAMS");
    }

}
