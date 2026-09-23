package com.sonkim.bookmarking.common.s3.service;

public record S3FileDeletionRequestedEvent(String prefix, String fileKey) {
}
