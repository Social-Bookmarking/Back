package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.service.YoutubeService;
import com.sonkim.bookmarking.common.util.YoutubeUrlUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class YoutubeOgStrategy implements OgExtractorStrategy{

    private final YoutubeService youtubeService;

    @Override
    public boolean supports(String url) {
        return url.contains("youtu.be") || url.contains("youtube.com");
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("Youtube URL 감지. Youtube Data API 이용하여 정보 추출");
        String videoId = YoutubeUrlUtil.extractVideoId(url);
        if (videoId != null) {
            return youtubeService.getVideoDetails(videoId);
        }
        return new BookmarkOGDto();
    }
}
