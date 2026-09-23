package com.sonkim.bookmarking.common.util;

import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenGraphImageRefreshService {
    private final OpenGraphImageCache imageCache;
    private final OGUtil ogUtil;

    @Async("openGraphExecutor")
    public void refreshAsync(String pageUrl) {
        if (imageCache.get(pageUrl) != null) {
            return;
        }
        String lockToken = imageCache.tryAcquireRefreshLock(pageUrl);
        if (lockToken == null) {
            return;
        }

        try {
            if (imageCache.get(pageUrl) != null) {
                return;
            }
            BookmarkOGDto ogData = ogUtil.getOpenGraphData(pageUrl);
            if (ogData != null && ogData.getImage() != null && !ogData.getImage().isBlank()) {
                String imageUrl = RemoteImageUrlValidator.validate(ogData.getImage());
                imageCache.put(pageUrl, imageUrl);
            }
        } catch (Exception e) {
            log.warn("OpenGraph 이미지 링크 비동기 갱신 실패. url={}", pageUrl, e);
        } finally {
            imageCache.releaseRefreshLock(pageUrl, lockToken);
        }
    }
}
