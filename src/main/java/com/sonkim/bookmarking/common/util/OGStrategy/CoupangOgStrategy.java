package com.sonkim.bookmarking.common.util.OGStrategy;

import com.sonkim.bookmarking.common.util.OGUtil;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
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
@Order(5)
public class CoupangOgStrategy implements OgExtractorStrategy{
    private final GenericObjectPool<WebDriver> driverPool;

    @Override
    public boolean supports(String url) {
        return url.contains("coupang.com");
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("Coupang URL 감지. 동적 콘텐츠 로딩 후 정보 추출");
        WebDriver driver = null;
        try {
            driver = driverPool.borrowObject();
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("meta[property='og:title']")));

            Document doc = Jsoup.parse(driver.getPageSource());
            return OGUtil.extractOgTags(doc);

        } catch (TimeoutException e) {
            log.warn("Coupang 정보 추출 중 Timeout 발생: {}", e.getMessage());

            // 🔽 타임아웃 발생 시점의 HTML을 로그로 출력합니다.
            if (driver != null) {
                String pageSourceOnTimeout = driver.getPageSource();
                log.warn("Timeout 발생 시점의 HTML:\n{}", pageSourceOnTimeout);

                // 타임아웃이 발생했더라도 현재 페이지 소스에서 OG 태그 추출을 시도합니다.
                Document doc = Jsoup.parse(pageSourceOnTimeout);
                return OGUtil.extractOgTags(doc);
            }
        } catch (Exception e) {
            log.error("Coupang 정보 추출 중 예상치 못한 오류 발생", e);
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
