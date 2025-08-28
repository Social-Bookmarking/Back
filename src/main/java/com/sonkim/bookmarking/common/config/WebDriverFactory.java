package com.sonkim.bookmarking.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Slf4j
@RequiredArgsConstructor
public class WebDriverFactory extends BasePooledObjectFactory<WebDriver> {

    private final ChromeOptions options;

    // 새로운 WebDriver 인스턴스 생성
    @Override
    public WebDriver create() {
        log.info(">>>>> WebDriver 인스턴스 생성");
        return new ChromeDriver(options);
    }

    // 객체를 풀에서 관리할 수 있도록 래핑
    @Override
    public PooledObject<WebDriver> wrap(WebDriver webDriver) {
        return new DefaultPooledObject<>(webDriver);
    }

    // 사용이 끝난 WebDriver 인스턴스 제거
    @Override
    public void destroyObject(PooledObject<WebDriver> p) {
        log.info(">>>> WebDriver 인스턴스 폐기");
        p.getObject().quit();
    }

    @Override
    public boolean validateObject(PooledObject<WebDriver> p) {
        try {
            p.getObject().getTitle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
