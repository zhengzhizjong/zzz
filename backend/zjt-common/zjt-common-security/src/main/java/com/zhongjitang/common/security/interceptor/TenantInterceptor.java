package com.zhongjitang.common.security.interceptor;

import com.zhongjitang.common.core.context.TenantContext;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 多租户拦截器
 * <p>
 * 校验当前请求是否携带租户信息，确保数据隔离。
 * 白名单路径直接放行，不进行租户校验。
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final List<String> ignoreUrls;

    /**
     * 请求预处理：校验租户信息
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true=继续执行, false=中断请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 白名单路径放行
        String requestPath = request.getRequestURI();
        if (isIgnoredPath(requestPath)) {
            return true;
        }

        // 校验租户信息
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("请求缺少租户信息: path={}", requestPath);
            throw new BusinessException(ErrorCode.TENANT_EXPIRED);
        }

        return true;
    }

    /**
     * 判断路径是否在白名单中
     *
     * @param path 请求路径
     * @return true=在白名单中, false=不在
     */
    private boolean isIgnoredPath(String path) {
        if (ignoreUrls == null || ignoreUrls.isEmpty()) {
            return false;
        }
        return ignoreUrls.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
