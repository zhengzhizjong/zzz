package com.zhongjitang.common.security.interceptor;

import com.zhongjitang.common.core.context.TenantContext;
import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 认证拦截器
 * <p>
 * 从请求Header中提取JWT Token，解析并设置用户上下文和租户上下文。
 * 白名单路径直接放行，不进行Token校验。
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final List<String> ignoreUrls;

    /**
     * 请求预处理：解析JWT Token，设置用户和租户上下文
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

        // 从Header获取Token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 解析Token并设置上下文
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            String userType = jwtUtil.getUserType(token);
            Long tenantId = jwtUtil.getTenantId(token);
            Long storeId = jwtUtil.getStoreId(token);

            // 设置用户上下文
            UserContext.UserInfo userInfo = new UserContext.UserInfo(userId, username, userType, storeId);
            UserContext.set(userInfo);

            // 设置租户上下文
            TenantContext.TenantInfo tenantInfo = new TenantContext.TenantInfo(tenantId, storeId, null);
            TenantContext.set(tenantInfo);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.warn("Token无效: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 请求完成后清理上下文
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
        TenantContext.clear();
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
