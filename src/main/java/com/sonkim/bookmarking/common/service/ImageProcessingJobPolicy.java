package com.sonkim.bookmarking.common.service;

import java.time.Duration;

public final class ImageProcessingJobPolicy {
    public static final int MAX_ATTEMPTS = 3;
    public static final int RECOVERY_BATCH_SIZE = 20;
    public static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(30);
    public static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(20);

    private ImageProcessingJobPolicy() {
    }
}
