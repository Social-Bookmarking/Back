package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import com.sonkim.bookmarking.domain.profile.entity.Profile;
import com.sonkim.bookmarking.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImageProcessingJobStateService {
    private final ImageProcessingJobRepository jobRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ProfileRepository profileRepository;

    public record Work(
            Long id,
            ImageProcessingJob.TargetType targetType,
            String sourceFileKey,
            int attempt) {}
    public record Completion(boolean applied, String oldFileKey, boolean cleanupResult) {}

    @Transactional
    public Work start(Long jobId) {
        ImageProcessingJob job = jobRepository.findByIdForUpdate(jobId).orElse(null);
        if (job == null || job.getStatus() != ImageProcessingJob.Status.PENDING) {
            return null;
        }
        if (job.getEffectiveSourceType() == ImageProcessingJob.SourceType.REMOTE_URL) {
            job.cancel();
            return null;
        }
        job.start();
        return new Work(
                job.getId(), job.getTargetType(), job.getSourceFileKey(), job.getAttemptCount());
    }

    @Transactional
    public Completion complete(Long jobId, int attempt, String resultFileKey) {
        ImageProcessingJob job = jobRepository.findByIdForUpdate(jobId).orElseThrow();
        if (job.getStatus() != ImageProcessingJob.Status.PROCESSING || job.getAttemptCount() != attempt) {
            return new Completion(false, null, false);
        }

        String oldFileKey = null;
        boolean applied = false;
        if (job.getTargetType() == ImageProcessingJob.TargetType.BOOKMARK) {
            Bookmark bookmark = bookmarkRepository.findByIdForImageUpdate(job.getTargetId()).orElse(null);
            boolean isCurrentRequest = bookmark != null
                    && Objects.equals(bookmark.getPendingImageKey(), job.getSourceFileKey());
            if (isCurrentRequest) {
                oldFileKey = bookmark.getImageKey();
                bookmark.updateImageKey(resultFileKey);
                bookmark.updatePendingImageKey(null);
                applied = true;
            }
        } else {
            Profile profile = profileRepository.findByIdForImageUpdate(job.getTargetId()).orElse(null);
            if (profile != null && Objects.equals(profile.getPendingImageKey(), job.getSourceFileKey())) {
                oldFileKey = profile.getImageKey();
                profile.updateImageKey(resultFileKey);
                profile.updatePendingImageKey(null);
                applied = true;
            }
        }

        if (applied) {
            job.succeed(resultFileKey);
        } else {
            job.cancel();
        }
        return new Completion(applied, oldFileKey, !applied);
    }

    @Transactional
    public void fail(Long jobId, int attempt, Exception error) {
        ImageProcessingJob job = jobRepository.findByIdForUpdate(jobId).orElse(null);
        if (job != null && job.getStatus() == ImageProcessingJob.Status.PROCESSING
                && job.getAttemptCount() == attempt) {
            LocalDateTime nextAttemptAt = null;
            if (attempt < ImageProcessingJobPolicy.MAX_ATTEMPTS) {
                long multiplier = 1L << Math.max(0, attempt - 1);
                nextAttemptAt = LocalDateTime.now().plus(
                        ImageProcessingJobPolicy.INITIAL_RETRY_DELAY.multipliedBy(multiplier));
            }
            job.fail(error.toString(), nextAttemptAt);
        }
    }

    @Transactional
    public boolean prepareForRecovery(Long jobId, LocalDateTime now, LocalDateTime staleBefore) {
        ImageProcessingJob job = jobRepository.findByIdForUpdate(jobId).orElse(null);
        if (job == null) {
            return false;
        }
        if (job.getEffectiveSourceType() == ImageProcessingJob.SourceType.REMOTE_URL) {
            job.cancel();
            return false;
        }

        if (job.getStatus() == ImageProcessingJob.Status.PENDING) {
            return job.getAttemptCount() < ImageProcessingJobPolicy.MAX_ATTEMPTS;
        }
        if (job.getStatus() == ImageProcessingJob.Status.FAILED
                && job.getAttemptCount() < ImageProcessingJobPolicy.MAX_ATTEMPTS
                && job.getNextAttemptAt() != null
                && !job.getNextAttemptAt().isAfter(now)) {
            job.prepareRetry(job.getLastError());
            return true;
        }
        if (job.getStatus() == ImageProcessingJob.Status.PROCESSING
                && !job.getUpdatedAt().isAfter(staleBefore)) {
            if (job.getAttemptCount() >= ImageProcessingJobPolicy.MAX_ATTEMPTS) {
                job.fail("처리 제한 시간을 초과했고 최대 시도 횟수에 도달했습니다.", null);
                return false;
            }
            job.prepareRetry("처리 제한 시간을 초과하여 다시 시도합니다.");
            return true;
        }
        return false;
    }
}
