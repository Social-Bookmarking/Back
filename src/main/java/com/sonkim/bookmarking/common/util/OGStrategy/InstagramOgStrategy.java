package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
public class InstagramOgStrategy implements OgExtractorStrategy {
    private final GenericObjectPool<WebDriver> driverPool;

    @Override
    public boolean supports(String url) {
        return url.contains("instagram.com");
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("Instagram URL 감지. 동적 컨텐츠 로딩 후 추출");
        WebDriver driver = null;
        try {
            driver = driverPool.borrowObject();
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("meta[property='og:title']")));
            if (driver.getPageSource() != null) {
                Document doc = Jsoup.parse(driver.getPageSource());
                return OGUtil.extractOgTags(doc);
            }
        } catch (Exception e) {
            // 로그인이 필요한 비공개 게시물이거나, 존재하지 않는 게시물일 경우 타임아웃이 발생할 수 있습니다.
            log.warn("Instagram 정보 추출 중 오류 발생 (비공개 게시물일 수 있음): {}", e.getMessage());
            // 타임아웃 시 기본 정보라도 반환 시도
            if(driver != null) {
                Document doc = Jsoup.parse(driver.getPageSource());
                return OGUtil.extractOgTags(doc);
            }
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
