package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.common.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCacheService {

    private final S3Service s3Service;

    @Cacheable(value = "imageUrlToKey", key = "#imageUrl")
    public String getFileKeyFromImageUrl(String imageUrl) {
        log.info(">>>> 캐시 미스. S3에 이미지 업로드... URL: {}", imageUrl);
        try (InputStream in = new URL(imageUrl).openStream()) {
            // URL에서 이미지를 byte[]로 읽어옴
            byte[] imageBytes = in.readAllBytes();

            // 파일 이름 추출
            String fileNameWithQuery = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            int queryIndex = fileNameWithQuery.indexOf('?');
            String originalFileName = (queryIndex != -1) ? fileNameWithQuery.substring(0, queryIndex) : fileNameWithQuery;

            // S3에 업로드하고 fileKey 반환
            return s3Service.uploadImageBytes(imageBytes, originalFileName, "bookmarks/");

        } catch (IOException e) {
            throw new RuntimeException("이미지 다운로드 및 업로드 실패");
        }
    }
}
