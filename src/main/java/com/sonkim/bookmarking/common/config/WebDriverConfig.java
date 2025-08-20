package com.sonkim.bookmarking.common.config;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class WebDriverConfig {

    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--lang=ko-KR");
        return options;
    }

    @Bean(destroyMethod = "close")  // 앱 종료 시 풀도 같이 종료
    public GenericObjectPool<WebDriver> webDriverPool(ChromeOptions options) {
        GenericObjectPoolConfig<WebDriver> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(5);    // 최대 드라이버 인스턴스 수
        config.setMinIdle(2);     // 최소 유휴 인스턴스 수
        config.setJmxEnabled(false);
        config.setTimeBetweenEvictionRuns(Duration.ofMinutes(10));          // 10분마다 유휴 객체 검사
        config.setSoftMinEvictableIdleDuration(Duration.ofMinutes(30));     // 30분 이상 사용되지 않은 객체는 풀에서 제거

        WebDriverFactory factory = new WebDriverFactory(options);
        return new GenericObjectPool<>(factory, config);
    }
}
