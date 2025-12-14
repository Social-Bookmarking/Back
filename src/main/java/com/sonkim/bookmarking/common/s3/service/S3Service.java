package com.sonkim.bookmarking.common.s3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonkim.bookmarking.common.s3.dto.PresignedUrlDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.lambda.function-name}")
    private String lambdaFunctionName;

    public S3Service(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName, S3Presigner s3Presigner, LambdaClient lambdaClient, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Presigner = s3Presigner;
        this.lambdaClient = lambdaClient;
        this.objectMapper = objectMapper;
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
        try {
            // Lambda에 보낼 데이터 생성
            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("prefix", prefix);
            payloadMap.put("fileName", fileName);
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

            // 앞뒤 쌍따옴표 제거
            String newFileName = responseString.replace("\"", "");

            log.info("AWS Lambda 처리 완료. 변환된 파일명: {}", newFileName);
            return newFileName;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lambda 호출 Payload 생성 실패", e);
        } catch (Exception e) {
            throw new RuntimeException("이미지 처리 서버(Lambda) 호출 중 오류 발생", e);
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
}
