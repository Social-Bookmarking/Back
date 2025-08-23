package com.sonkim.bookmarking.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CleanupScheduler {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupTempFiles() {
        log.info("S3 임시 폴더 정리 작업 시작...");

        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix("temp/")
                .build();

        // 생성된지 24시간이 지난 파일들은 삭제
        s3Client.listObjects(listRequest).contents().stream()
                .filter(obj -> obj.lastModified().isBefore(Instant.now().minus(1, ChronoUnit.DAYS)))
                .forEach(obj -> {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName).key(obj.key()).build();
                    s3Client.deleteObject(deleteRequest);
                    log.info("오래된 임시 파일 삭제: {}", obj.key());
                });

        log.info("S3 임시 폴더 정리 작업 완료.");
    }
}
