package com.sonkim.bookmarking.common.service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "image_processing_job")
public class ImageProcessingJob {
    public enum TargetType { BOOKMARK, PROFILE }
    public enum SourceType { UPLOAD, REMOTE_URL }
    public enum Status { PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SourceType sourceType;

    @Column(length = 1024)
    private String sourceFileKey;

    @Column(columnDefinition = "TEXT")
    private String sourceImageUrl;

    @Column(length = 1024)
    private String requestedResultFileKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int attemptCount;

    @Column(length = 1024)
    private String resultFileKey;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime nextAttemptAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ImageProcessingJob(TargetType targetType, Long targetId, String sourceFileKey) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.sourceType = SourceType.UPLOAD;
        this.sourceFileKey = sourceFileKey;
    }

    public SourceType getEffectiveSourceType() {
        return sourceType == null ? SourceType.UPLOAD : sourceType;
    }

    public void start() {
        status = Status.PROCESSING;
        attemptCount++;
        lastError = null;
        nextAttemptAt = null;
        updatedAt = LocalDateTime.now();
    }

    public void succeed(String resultFileKey) {
        status = Status.SUCCEEDED;
        this.resultFileKey = resultFileKey;
        updatedAt = LocalDateTime.now();
    }

    public void fail(String error, LocalDateTime nextAttemptAt) {
        status = Status.FAILED;
        lastError = error;
        this.nextAttemptAt = nextAttemptAt;
        updatedAt = LocalDateTime.now();
    }

    public void prepareRetry(String reason) {
        status = Status.PENDING;
        lastError = reason;
        nextAttemptAt = null;
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        status = Status.CANCELLED;
        updatedAt = LocalDateTime.now();
    }
}
