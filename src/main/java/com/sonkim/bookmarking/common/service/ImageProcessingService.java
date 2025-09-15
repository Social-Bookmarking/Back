package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.common.s3.service.S3Service;
import com.sonkim.bookmarking.domain.bookmark.entity.Bookmark;
import com.sonkim.bookmarking.domain.bookmark.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {
    private final BookmarkRepository bookmarkRepository;
    private final S3Service s3Service;

    @Async("imageUploadExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void downloadAndUploadToS3(BookmarkCreatedEvent event) {
        Long bookmarkId = event.getBookmarkId();
        String imageUrl = event.getImageUrl();

        log.info("북마크 ID: {}, 이미지 처리 시작... URL: {}", bookmarkId, imageUrl);
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.info("이미지 URL이 없어 작업을 종료합니다.");
            return;
        }

        try {
            // S3에 업로드 후 파일 키 수령
            String fileKey;
            try (InputStream in = new URL(imageUrl).openStream()) {
                // URL에서 이미지를 byte[]로 읽어옴
                byte[] imageBytes = in.readAllBytes();

                // 파일 이름 추출
                String fileNameWithQuery = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                int queryIndex = fileNameWithQuery.indexOf('?');
                String originalFileName = (queryIndex != -1) ? fileNameWithQuery.substring(0, queryIndex) : fileNameWithQuery;

                // S3에 업로드하고 fileKey 반환
                fileKey = s3Service.uploadImageBytes(imageBytes, originalFileName, "bookmarks/");
            }

            // 해당 북마크 이미지 url 수정
            Optional<Bookmark> bookmarkOptional = bookmarkRepository.findById(bookmarkId);

            if (bookmarkOptional.isPresent()) {
                Bookmark bookmark = bookmarkOptional.get();
                bookmark.updateImageKey(fileKey);
                bookmark.updateOriginalImageUrl(null);
                log.info("북마크 ID {}: 이미지 키 비동기 업데이트 완료. Key: {}", bookmarkId, fileKey);
            } else {
                // 북마크를 찾지 못했을 경우: 경고 로그 기록
                log.warn("비동기 이미지 처리 후 북마크를 찾을 수 없습니다. Bookmark ID: {}. S3 Key: {}", bookmarkId, fileKey);
            }
        } catch (Exception e) {
            log.error("북마크 ID : {}, 이미지 처리 중 오류 발생", bookmarkId, e);
        }
    }
}
