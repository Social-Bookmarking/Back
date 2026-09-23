package com.sonkim.bookmarking.common.s3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonkim.bookmarking.common.s3.dto.PresignedUrlDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    static final long MAX_UPLOAD_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_UPLOAD_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp");

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.lambda.function-name}")
    private String lambdaFunctionName;

    @Value("${cloudflare.cdn.domain}")
    private String cloudFrontDomain;

    public S3Service(S3Client s3Client, @Value("${cloud.r2.bucket}") String bucketName, S3Presigner s3Presigner, LambdaClient lambdaClient, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Presigner = s3Presigner;
        this.lambdaClient = lambdaClient;
        this.objectMapper = objectMapper;
    }

    public String generateImageUrl(String prefix, String key) {
        try {
            String normalizedKey = removeLeadingSlashes(key);
            String encodedKey = URLEncoder.encode(normalizedKey, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return joinImageUrl(prefix, encodedKey);
        } catch (Exception e) {
            log.error("Failed to encode key: {}", key, e);
            return joinImageUrl(prefix, key);
        }
    }

    private String joinImageUrl(String prefix, String key) {
        String domain = removeTrailingSlashes(cloudFrontDomain);
        String normalizedPrefix = removeSurroundingSlashes(prefix);
        String normalizedKey = removeLeadingSlashes(key);
        return domain + "/" + normalizedPrefix + "/" + normalizedKey;
    }

    private String removeTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private String removeLeadingSlashes(String value) {
        int start = 0;
        while (start < value.length() && value.charAt(start) == '/') {
            start++;
        }
        return value.substring(start);
    }

    private String removeSurroundingSlashes(String value) {
        return removeTrailingSlashes(removeLeadingSlashes(value));
    }

    public PresignedUrlDto generatePresignedPutUrl(String fileName) {
        String extension = getAllowedUploadExtension(fileName);
        // 사용자 파일명 대신 UUID와 검증된 확장자만 객체 키에 사용한다.
        String key = UUID.randomUUID() + "." + extension;
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
        Map<String, String> payload = new HashMap<>();
        payload.put("prefix", prefix);
        payload.put("fileName", fileName);
        return invokeImageLambda(payload);
    }

    private String invokeImageLambda(Map<String, String> payloadMap) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payloadMap);

            // Lambda 호출 요청 생성
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaFunctionName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            log.info("AWS Lambda 호출 시작: Function={}, Payload={}", lambdaFunctionName, jsonPayload);

            // 호출. 완료될 때까지 대기
            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);

            // 완료 후 전달된 응답에 담긴 파일 이름 가져오기
            String responseString = invokeResponse.payload().asUtf8String();

            // 에러 체크 (Lambda 실행 에러)
            if (invokeResponse.functionError() != null) {
                log.error("Lambda 실행 오류: {}", responseString);
                throw new RuntimeException("이미지 처리 Lambda 실행 중 오류 발생: " + responseString);
            }

            String newFileName = objectMapper.readValue(responseString, String.class);
            if (newFileName == null || newFileName.isBlank()) {
                throw new RuntimeException("Lambda가 변환된 이미지 키를 반환하지 않았습니다.");
            }

            log.info("AWS Lambda 처리 완료. 변환된 파일명: {}", newFileName);
            return newFileName;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lambda 호출 Payload 생성 실패", e);
        } catch (Exception e) {
            throw new RuntimeException("이미지 처리 서버(Lambda) 호출 중 오류 발생", e);
        }
    }

    public void verifyUploadedFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("이미지 파일 키가 필요합니다.");
        }

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key("temp/" + fileName)
                    .build();
            var object = s3Client.headObject(request);
            if (object.contentLength() == null || object.contentLength() == 0) {
                throw new IllegalArgumentException("업로드된 이미지 파일이 비어 있습니다.");
            }
            if (object.contentLength() > MAX_UPLOAD_SIZE_BYTES) {
                throw new IllegalArgumentException("이미지 파일은 10MB를 초과할 수 없습니다.");
            }
            if (object.contentType() == null
                    || !ALLOWED_UPLOAD_TYPES.containsValue(object.contentType().toLowerCase())) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
            }
            log.info("업로드 이미지 확인 완료. Bucket={}, Key={}, Size={}",
                    bucketName, request.key(), object.contentLength());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new IllegalArgumentException("업로드된 이미지 파일을 찾을 수 없습니다.", e);
            }
            throw e;
        }
    }

    private String getAllowedUploadExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("이미지 파일 이름이 필요합니다.");
        }
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("이미지 파일 확장자가 필요합니다.");
        }
        String extension = fileName.substring(extensionIndex + 1).toLowerCase();
        if (!ALLOWED_UPLOAD_TYPES.containsKey(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 확장자입니다.");
        }
        return extension;
    }

    public void deleteFile(String prefix, String key) {
        String sourceKey = prefix + key;

        s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(sourceKey));
    }

}
