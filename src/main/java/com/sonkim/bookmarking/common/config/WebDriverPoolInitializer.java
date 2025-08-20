package com.sonkim.bookmarking.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebDriverPoolInitializer {

    private final GenericObjectPool<WebDriver> webDriverPool;

    // @PostConstruct: 모든 빈(Bean)이 초기화된 후 딱 한 번 실행되는 메서드
    @PostConstruct
    public void initializePool() {
        try {
            log.info("WebDriver Pool 초기화를 시작합니다... (minIdle 만큼 인스턴스 생성)");
            // 풀 설정에 정의된 minIdle 개수만큼 객체를 미리 생성하여 풀을 채웁니다.
            webDriverPool.addObjects(webDriverPool.getMinIdle());
            log.info("WebDriver Pool 초기화 완료.");
        } catch (Exception e) {
            log.error("WebDriver Pool 초기화 중 오류 발생", e);
        }
    }
}
