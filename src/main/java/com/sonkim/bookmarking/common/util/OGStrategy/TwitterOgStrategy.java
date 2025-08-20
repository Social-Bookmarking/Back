package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class TwitterOgStrategy implements OgExtractorStrategy{
    private final GenericObjectPool<WebDriver> driverPool;

    @Override
    public boolean supports(String url) {
        return url.contains("twitter.com") || url.contains("x.com");
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("Twitter(X) URL 감지. 동적 컨텐츠 로딩 후 추출");
        WebDriver driver = null;
        try {
            driver = driverPool.borrowObject();
            driver.get(url);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("X")));
            } catch (TimeoutException e) {
                log.warn("민감한 콘텐츠이거나 로그인이 필요할 수 있습니다. URL: {}", url);
            }

            if (driver.getPageSource() != null) {
                Document doc = Jsoup.parse(driver.getPageSource());
                return OGUtil.extractOgTags(doc);
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenGraph 정보 추출 중 오류 발생", e);
        } finally {
            if (driver != null) {
                try {
                    driverPool.returnObject(driver);
                } catch (Exception e) {
                    log.error("드라이버 반환 중 오류 발생", e);
                }
            }
        }

        return new BookmarkOGDto();
    }
}