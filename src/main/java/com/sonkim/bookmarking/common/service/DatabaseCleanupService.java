package com.sonkim.bookmarking.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void runCleanup(String taskType) {
        log.info(">>>>> Scheduler Job Start: {}", taskType);
        int deletedCount;

        switch (taskType) {
            case "CLEANUP_UNUSED_TAGS" -> deletedCount = deleteUnusedTags();
            case "CLEANUP_PENDING_TEAMS" -> deletedCount = deletePendingTeams();
            case "CLEANUP_ORPHANED_TEAMS" -> deletedCount = deleteOrphanedTeams();
            default -> throw new IllegalArgumentException("Invalid taskType: " + taskType);
        }

        log.info("TaskType: {} Completed, Deleted: {}", taskType, deletedCount);
    }

    private int deleteUnusedTags() {
        String sql = "DELETE t FROM tag t " +
                "LEFT JOIN bookmark_tag bt ON t.id = bt.tag_id " +
                "WHERE bt.tag_id IS NULL";

        return jdbcTemplate.update(sql);
    }

    private int deletePendingTeams() {
        String sql = "DELETE FROM team " +
                "WHERE status = 'PENDING_DELETION' " +
                "AND deletion_scheduled_at < NOW()";

        return jdbcTemplate.update(sql);
    }

    private int deleteOrphanedTeams() {
        String sql = "DELETE t FROM team t " +
                "WHERE NOT EXISTS (" +
                "  SELECT 1 " +
                "  FROM team_member tm " +
                "  JOIN user u ON tm.account_id = u.id " +
                "  WHERE tm.team_id = t.id " +
                "  AND u.user_status = 'ACTIVE'" +
                ")";

        return jdbcTemplate.update(sql);
    }
}
