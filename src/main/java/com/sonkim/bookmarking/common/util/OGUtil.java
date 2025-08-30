package com.sonkim.bookmarking.common.util;

import com.sonkim.bookmarking.common.util.OGStrategy.OgExtractorStrategy;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OGUtil {

    private final List<OgExtractorStrategy> strategies;

    @Cacheable(value = "ogData", key = "#url")
    public BookmarkOGDto getOpenGraphData(String url) {
        log.info(">>>> OG Data Caching... URL: {}", url);
        return strategies.stream()
                .filter(s -> s.supports(url))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("지원하는 OG 추출 전략이 없습니다."))
                .extract(url);
    }

    public static BookmarkOGDto extractOgTags(Document doc) {
        BookmarkOGDto dto = new BookmarkOGDto();

        // "meta[property^=og:]"는 property 속성이 "og:"로 시작하는 모든 meta 태그를 선택합니다.
        Elements ogTags = doc.select("meta[property^=og:]");

        for (Element tag : ogTags) {
            String property = tag.attr("property");
            String content = tag.attr("content");

            switch (property) {
                case "og:title":
                    dto.setTitle(content);
                    break;
                case "og:description":
                    dto.setDescription(content);
                    break;
                case "og:image":
                    dto.setImage(content);
                    break;
            }
        }

        // 만약 og:title이 없다면, 일반 title 태그라도 가져옵니다.
        if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
            dto.setTitle(doc.title());
        }

        return dto;
    }
}
