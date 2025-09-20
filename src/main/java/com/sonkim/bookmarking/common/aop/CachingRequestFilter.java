package com.sonkim.bookmarking.common.aop;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Integer.MIN_VALUE)
public class CachingRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        // POST 요청에만 ContextCachingRequestWrapper 적용
        if ("POST".equals(httpServletRequest.getMethod())) {
            // ContextCachingRequestWrapper로 요청 내용 캐싱
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(httpServletRequest);

            chain.doFilter(wrapperRequest, response);
        }
        else {
            // 이외의 요청은 캐싱하지 않고 곧바로 진행
            chain.doFilter(request, response);
        }
    }
}
