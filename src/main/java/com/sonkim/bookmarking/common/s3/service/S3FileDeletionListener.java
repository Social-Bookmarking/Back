package com.sonkim.bookmarking.common.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileDeletionListener {
    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteAfterCommit(S3FileDeletionRequestedEvent event) {
        try {
            s3Service.deleteFile(event.prefix(), event.fileKey());
        } catch (Exception e) {
            log.warn("DB 커밋 후 이미지 파일 삭제에 실패했습니다. prefix={}, fileKey={}",
                    event.prefix(), event.fileKey(), e);
        }
    }
}
