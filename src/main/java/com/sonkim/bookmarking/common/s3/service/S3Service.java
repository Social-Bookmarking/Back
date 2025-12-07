package com.sonkim.bookmarking.common.s3.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import com.sonkim.bookmarking.common.s3.dto.PresignedUrlDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Presigner = s3Presigner;
    }

    public URL generatePresignedGetUrl(String prefix, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(prefix + key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url();
    }

    public PresignedUrlDto generatePresignedPutUrl(String fileName) {
        // 파일 이름이 중복되지 않도록 UUID 사용
        String key = UUID.randomUUID() + "_" + fileName;
        String fullKey = "temp/" + key;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fullKey)
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(putObjectPresignRequest);

        return PresignedUrlDto.builder()
                .presignedUrl(presignedPutObjectRequest.url().toString())
                .fileKey(key)
                .build();
    }

    public String moveFileToPermanentStorage(String prefix, String fileName) {
        String sourceKey = "temp/" + fileName;

        try {
            // S3 임시 폴더에서 이미지 다운로드
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceKey)
                    .build();
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getRequest);
            byte[] originalBytes = responseBytes.asByteArray();

            // 리사이징 및 WebP로 변환
            ImmutableImage image = ImmutableImage.loader().fromBytes(originalBytes);
            if (image.width > 600) {
                image = image.scaleToWidth(600);
            }
            byte[] convertedBytes = image.bytes(WebpWriter.DEFAULT.withQ(80));

            // 새로운 파일 이름 생성 (확장자를 .webp로 변경)
            String newFileName = getFileNameWithoutExtension(fileName) + ".webp";
            String destKey = prefix  + newFileName;

            // 영구 저장소에 이미지 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(destKey)
                    .contentType("image/webp")
                    .contentDisposition("inline")
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(convertedBytes));

            // 임시 저장소의 원본 파일 제거
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceKey)
                    .build());

            log.info("이미지 변환 및 이동 완료: {} -> {}", sourceKey, destKey);
            return newFileName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 및 이동 중 오류 발생: " + fileName, e);
        }
    }

    public void deleteFile(String prefix, String key) {
        String sourceKey = prefix + key;

        s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(sourceKey));
    }

    public String uploadImageBytes(byte[] imageBytes, String fileName, String prefix) {
        // 파일 이름이 중복되지 않도록 UUID 사용
        String key = UUID.randomUUID() + "_" + fileName;
        String fullKey = prefix + key;

        // 확장자 인식
        String contentType = getContentType(fileName);

        // S3에 업로드할 객체 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fullKey)
                .contentType(contentType)
                .contentDisposition("inline")
                .build();

        // 파일 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

        return key;
    }

    private String getContentType(String fileName) {
        // 파일 이름에서 마지막 '.' 이후의 문자열을 확장자로 간주
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default ->
                // 알려지지 않은 확장자는 일반적인 바이너리 파일 타입으로 처리
                    "application/octet-stream";
        };
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }
}
