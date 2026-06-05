package com.zhongjitang.common.log.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 请求ID过滤器
 * <p>
 * 为每个请求生成唯一的requestId，设置到MDC中便于日志追踪，
 * 同时设置到响应Header中供前端排查问题。
 * </p>
 */
@Slf4j
@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    /** MDC中的requestId键名 */
    private static final String MDC_REQUEST_ID = "requestId";

    /** 响应Header中的requestId键名 */
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * 过滤器内部处理
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = generateRequestId();

        try {
            // 设置到MDC
            MDC.put(MDC_REQUEST_ID, requestId);

            // 设置到响应Header
            response.setHeader(HEADER_REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            // 清除MDC
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * 生成请求ID
     *
     * @return UUID格式的请求ID（去除横线）
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
