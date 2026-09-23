package com.sonkim.bookmarking.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingJobRecoveryScheduler {
    private final ImageProcessingJobRepository jobRepository;
    private final ImageProcessingJobStateService jobStateService;
    private final ImageProcessingJobProcessor processor;

    @Scheduled(initialDelayString = "${image.processing.recovery.initial-delay-ms:30000}",
            fixedDelayString = "${image.processing.recovery.fixed-delay-ms:30000}")
    public void recover() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleBefore = now.minus(ImageProcessingJobPolicy.PROCESSING_TIMEOUT);
        List<Long> candidateIds = jobRepository.findRecoveryCandidateIds(
                ImageProcessingJob.Status.PENDING,
                ImageProcessingJob.Status.FAILED,
                ImageProcessingJob.Status.PROCESSING,
                ImageProcessingJobPolicy.MAX_ATTEMPTS,
                now,
                staleBefore,
                PageRequest.of(0, ImageProcessingJobPolicy.RECOVERY_BATCH_SIZE));

        for (Long jobId : candidateIds) {
            try {
                if (jobStateService.prepareForRecovery(jobId, now, staleBefore)) {
                    processor.processAsync(jobId);
                }
            } catch (Exception e) {
                log.error("이미지 처리 작업 복구 준비 실패. jobId={}", jobId, e);
            }
        }
    }
}
