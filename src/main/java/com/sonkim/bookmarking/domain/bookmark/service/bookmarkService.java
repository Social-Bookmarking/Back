package com.sonkim.bookmarking.domain.bookmark.service;

import com.sonkim.bookmarking.domain.bookmark.dto.bookmarkTestDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class bookmarkService {
    public bookmarkTestDto getOpenGraphData(String url) {
        // Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        bookmarkTestDto dto = new bookmarkTestDto();

        try {
            driver.get(url);
            String finalHtml;

            // 도메인에 따라 로직 분기
            if (url.contains("map.naver.com")) {
                // 네이버 지도 처리
                log.info("네이버 지도 URL 감지. 'entryIframe'을 이용하여 추출");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("entryIframe")));
                finalHtml = driver.getPageSource();

            } else if (url.contains("blog.naver.com")) {
                // 네이버 블로그 처리
                log.info("네이버 블로그 URL 감지. 'mainFrame'을 이용하여 추출");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("mainFrame")));
                finalHtml = driver.getPageSource();

            } else {
                // 그 외 일반 사이트 처리
                log.info("일반 URL 감지. 메인 페이지에서 정보 추출");
                finalHtml = driver.getPageSource();
            }

            // Jsoup으로 최종 HTML 파싱
            Document finalDoc = Jsoup.parse(finalHtml);
            dto = extractOgTags(finalDoc);

        } catch (Exception e) {
            log.error("OG 태그 추출 중 오류 발생: {}", e.getMessage());
        } finally {
            driver.quit();
        }

        return dto;
    }

    private bookmarkTestDto extractOgTags(Document doc) {
        bookmarkTestDto dto = new bookmarkTestDto();

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
                case "og:url":
                    dto.setUrl(content);
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
