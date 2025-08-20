package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(Integer.MAX_VALUE)
public class DefaultOgStrategy implements OgExtractorStrategy {
    @Override
    public boolean supports(String url) {
        return true;    // 모든 URL을 지원
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("일반 URL 감지. 메인 페이지에서 정보 추출");
        try {
            Document doc = Jsoup.connect(url).get();
            return OGUtil.extractOgTags(doc);
        } catch (IOException e) {
            throw new RuntimeException("OpenGraph 정보 추출 중 오류 발생", e);
        }
    }
}
