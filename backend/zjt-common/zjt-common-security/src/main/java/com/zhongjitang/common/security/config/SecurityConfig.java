package com.zhongjitang.common.security.config;

import com.zhongjitang.common.security.interceptor.AuthInterceptor;
import com.zhongjitang.common.security.interceptor.TenantInterceptor;
import com.zhongjitang.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 安全配置
 * <p>
 * 注册认证拦截器和租户拦截器，配置白名单路径。
 * 白名单路径可通过配置文件 security.ignore-urls 覆盖默认值。
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    /** 白名单路径，可通过配置文件覆盖 */
    @Value("${security.ignore-urls:/api/v1/auth/**,/api/v1/user/members/login,/api/v1/user/members/register,/api/v1/user/sms/send,/api/v1/user/employees/login,/swagger-ui/**,/v3/api-docs/**,/actuator/**,/health}")
    private String ignoreUrlsStr;

    /** 默认白名单路径 */
    private static final List<String> DEFAULT_IGNORE_URLS = Arrays.asList(
            "/api/v1/auth/**",
            "/api/v1/user/members/login",
            "/api/v1/user/members/register",
            "/api/v1/user/sms/send",
            "/api/v1/user/employees/login",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/health"
    );

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> ignoreUrls = resolveIgnoreUrls();

        // 认证拦截器
        registry.addInterceptor(new AuthInterceptor(jwtUtil, ignoreUrls))
                .addPathPatterns("/**")
                .order(1);

        // 租户拦截器
        registry.addInterceptor(new TenantInterceptor(ignoreUrls))
                .addPathPatterns("/**")
                .order(2);
    }

    /**
     * 解析白名单路径
     * <p>
     * 优先使用配置文件中的值，为空时使用默认值。
     * </p>
     *
     * @return 白名单路径列表
     */
    private List<String> resolveIgnoreUrls() {
        if (ignoreUrlsStr != null && !ignoreUrlsStr.trim().isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (String url : ignoreUrlsStr.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    urls.add(trimmed);
                }
            }
            return urls;
        }
        return DEFAULT_IGNORE_URLS;
    }
}
