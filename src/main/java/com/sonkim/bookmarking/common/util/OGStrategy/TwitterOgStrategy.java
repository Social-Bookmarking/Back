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
import java.util.Objects;

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
            log.info("WebDriver 빌림 (borrowObject). 해시코드: {}", driver.hashCode());

            driver.get(url);

            // TimeoutException 처리
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("X")));
            } catch (TimeoutException e) {
                log.warn("민감한 콘텐츠이거나 로그인이 필요할 수 있습니다. URL: {}", url);
            }

            // 현재 페이지 소스를 기반으로 DTO 추출
            Document doc = Jsoup.parse(Objects.requireNonNull(driver.getPageSource()));
            BookmarkOGDto dto = OGUtil.extractOgTags(doc);

            // 성공 시 드라이버 풀에 반납
            driverPool.returnObject(driver);
            log.info("WebDriver 반납 (returnObject). 해시코드: {}", driver.hashCode());
            driver = null;

            return dto;

        } catch (Exception e) {
            log.warn("Twitter OG 정보 추출 중 예외 발생: {}", e.getMessage());
            // 실패 시, 문제 발생 드라이버는 폐기
            if (driver != null) {
                try {
                    driverPool.invalidateObject(driver);
                    log.warn("문제 발생 WebDriver 폐기 (invalidateObject). 해시코드: {}", driver.hashCode());
                    driver = null;
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