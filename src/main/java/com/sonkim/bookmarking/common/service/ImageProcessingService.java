package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {
    private final S3Service s3Service;
    private final BookmarkRepository bookmarkRepository;

    @Async("imageUploadExecutor")
    @Transactional
    public void downloadAndUploadToS3(Long bookmarkId, String imageUrl) {
        log.info("북마크 ID: {}, 이미지 처리 시작... URL: {}", bookmarkId, imageUrl);
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.info("이미지 URL이 없어 작업을 종료합니다.");
            return;
        }

        try (InputStream in = new URL(imageUrl).openStream()) {
            // URL에서 이미지를 byte[]로 읽어옴
            byte[] imageBytes = in.readAllBytes();
            String fileNameWithQuery = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

            int queryIndex = fileNameWithQuery.indexOf('?');
            String originalFileName = (queryIndex != -1) ? fileNameWithQuery.substring(0, queryIndex) : fileNameWithQuery;

            // S3에 업로드 후 파일 키 수령
            String fileKey = s3Service.uploadImageBytes(imageBytes, originalFileName, "bookmarks/");

            // 해당 북마크 이미지 url 수정
            bookmarkRepository.findById(bookmarkId).ifPresent(bookmark -> {
                bookmark.updateImageKey(fileKey);
                bookmark.updateOriginalImageUrl(null);
            });


            log.info("북마크 ID: {}, 이미지 키 업데이트 완료. Key: {}", bookmarkId, fileKey);
        } catch (Exception e) {
            log.error("북마크 ID : {}, 이미지 처리 중 오류 발생", bookmarkId, e);
        }
    }

}
