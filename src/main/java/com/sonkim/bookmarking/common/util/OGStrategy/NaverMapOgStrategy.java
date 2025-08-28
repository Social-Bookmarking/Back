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
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class NaverMapOgStrategy implements OgExtractorStrategy{
    private final GenericObjectPool<WebDriver> driverPool;

    @Override
    public boolean supports(String url) {
        return url.contains("map.naver.com");
    }

    @Override
    public BookmarkOGDto extract(String url) {
        log.info("네이버 지도 URL 감지. 'entryIframe'을 이용하여 추출");
        WebDriver driver = null;
        try {
            driver = driverPool.borrowObject();
            log.info("WebDriver 빌림 (borrowObject). 해시코드: {}", driver.hashCode());

            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("entryIframe")));
            Document doc = Jsoup.parse(Objects.requireNonNull(driver.getPageSource()));
            BookmarkOGDto dto = OGUtil.extractOgTags(doc);

            driverPool.returnObject(driver);
            log.info("WebDriver 반납 (returnObject). 해시코드: {}", driver.hashCode());
            driver = null;

            return dto;

        } catch (Exception e) {
            if (driver != null) {
                try {
                    // 문제가 생겼을 수도 있는 인스턴스는 폐기
                    driverPool.invalidateObject(driver);
                    log.warn("문제 발생 WebDriver 폐기 (invalidateObject). 해시코드: {}", driver.hashCode());
                    driver = null; // 폐기 후 참조를 끊어줌
                } catch (Exception ex) {
                    log.error("오류 발생 드라이버 무효화 실패", ex);
                }
            }

            throw new RuntimeException("OpenGraph 정보 추출 중 오류 발생", e);
        } finally {
            if (driver != null) {
                try {
                    log.error("finally 블록에서 드라이버가 아직 처리되지 않음! 강제 반납. 해시코드: {}", driver.hashCode());
                    driverPool.returnObject(driver);
                } catch (Exception ex) {
                    log.error("드라이버 최종 반납 중 오류 발생", ex);
                }
            }
        }
    }
}
