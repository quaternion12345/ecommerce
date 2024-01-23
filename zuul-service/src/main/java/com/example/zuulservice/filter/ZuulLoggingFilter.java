package com.example.zuulservice.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class ZuulLoggingFilter extends ZuulFilter {
//    Logger logger = LoggerFactory.getLogger(ZuulLoggingFilter.class);

    // pre or post
    @Override
    public String filterType() {
        return "pre";
    }

    // 필터링 순서
    @Override
    public int filterOrder() {
        return 1;
    }

    // Filter로 사용여부
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // 실제 동작
    @Override
    public Object run() throws ZuulException {
        log.info("********** printing logs: ");
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info("********** " + request.getRequestURI());
        return null;
    }
}
