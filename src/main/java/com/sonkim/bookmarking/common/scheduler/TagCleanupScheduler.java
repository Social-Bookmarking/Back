package com.sonkim.bookmarking.common.scheduler;

import com.sonkim.bookmarking.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagCleanupScheduler  {
    private final TagService tagService;

    // 매일 새벽 4시 아무도 사용하지 않는 태그 정리
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupUnusedTags() {
        log.info("Cleaning up unused tags");
        tagService.deleteUnusedTags();
    }
}
