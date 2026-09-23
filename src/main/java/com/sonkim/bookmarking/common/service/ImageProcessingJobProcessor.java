package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.common.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingJobProcessor {
    private final ImageProcessingJobStateService jobStateService;
    private final S3Service s3Service;

    @Async("imageUploadExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ImageProcessingJobCreatedEvent event) {
        process(event.jobId());
    }

    @Async("imageUploadExecutor")
    public void processAsync(Long jobId) {
        process(jobId);
    }

    public void process(Long jobId) {
        ImageProcessingJobStateService.Work work = jobStateService.start(jobId);
        if (work == null) {
            return;
        }

        String prefix = work.targetType() == ImageProcessingJob.TargetType.BOOKMARK
                ? "bookmarks/" : "profile-images/";
        try {
            String resultFileKey = s3Service.moveFileToPermanentStorage(prefix, work.sourceFileKey());
            ImageProcessingJobStateService.Completion completion =
                    jobStateService.complete(jobId, work.attempt(), resultFileKey);
            if (completion.cleanupResult()) {
                deleteBestEffort(prefix, resultFileKey);
            } else if (completion.oldFileKey() != null
                    && !completion.oldFileKey().equals(resultFileKey)) {
                deleteBestEffort(prefix, completion.oldFileKey());
            }
        } catch (Exception e) {
            log.error("이미지 처리 작업 실패. jobId={}", jobId, e);
            jobStateService.fail(jobId, work.attempt(), e);
        }
    }

    private void deleteBestEffort(String prefix, String fileKey) {
        try {
            s3Service.deleteFile(prefix, fileKey);
        } catch (Exception e) {
            log.warn("이미지 파일 정리에 실패했습니다. prefix={}, fileKey={}", prefix, fileKey, e);
        }
    }
}
