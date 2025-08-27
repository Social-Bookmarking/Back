package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class NaverBlogOgStrategy implements OgExtractorStrategy {
    private final GenericObjectPool<WebDriver> driverPool;

    @Override
    public boolean supports(String url) {
        return url.contains("blog.naver.com") || url.contains("m.blog.naver.com");
    }

    @Override
    public BookmarkOGDto extract(String urlString) {
        log.info("네이버 블로그 URL 감지. 모바일 버전을 이용하여 태그 추출");

        String targetUrl = urlString;

        try {
            URL url = new URL(urlString);
            String host = url.getHost();

            if (host.equals("blog.naver.com")) {
                targetUrl = urlString.replaceFirst("blog.naver.com", "m.blog.naver.com");
            }

            Document doc = Jsoup.connect(targetUrl).get();
            return OGUtil.extractOgTags(doc);

        } catch (Exception e) {
            log.error("네이버 블로그 OG 태그 추출 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException("OpenGraph 정보 추출 중 오류 발생", e);
        }
    }
}
