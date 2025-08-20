package com.sonkim.bookmarking.common.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@RequiredArgsConstructor
public class WebDriverFactory extends BasePooledObjectFactory<WebDriver> {

    private final ChromeOptions options;

    // 새로운 WebDriver 인스턴스 생성
    @Override
    public WebDriver create() {
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
        p.getObject().close();
    }
}
