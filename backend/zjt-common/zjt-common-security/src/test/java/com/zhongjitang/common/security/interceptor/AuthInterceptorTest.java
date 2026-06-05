package com.zhongjitang.common.security.interceptor;

import com.zhongjitang.common.core.context.TenantContext;
import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.security.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 认证拦截器测试
 */
class AuthInterceptorTest {

    private AuthInterceptor authInterceptor;
    private JwtUtil jwtUtil;
    private HttpServletRequest request;
    private HttpServletRequest response;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "zhongjitang-default-secret-key-must-be-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(jwtUtil, "expireMinutes", 120L);
        jwtUtil.init();

        authInterceptor = new AuthInterceptor(jwtUtil, Arrays.asList("/api/v1/auth/**", "/swagger-ui/**"));
        request = mock(HttpServletRequest.class);
        response = mock(javax.servlet.http.HttpServletResponse.class);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        TenantContext.clear();
    }

    @Test
    @DisplayName("白名单路径应放行")
    void preHandle_ignorePath() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("无Authorization Header应抛出UNAUTHORIZED异常")
    void preHandle_noAuthHeader() {
        when(request.getRequestURI()).thenReturn("/api/v1/members/list");
        when(request.getHeader("Authorization")).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authInterceptor.preHandle(request, response, null));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("有效Token应设置上下文并放行")
    void preHandle_validToken() throws Exception {
        String token = jwtUtil.generateToken(1L, "admin", "admin", 100L, 200L);

        when(request.getRequestURI()).thenReturn("/api/v1/members/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        boolean result = authInterceptor.preHandle(request, response, null);
        assertTrue(result);

        // 验证上下文已设置
        assertEquals(1L, UserContext.getUserId());
        assertEquals("admin", UserContext.getUsername());
        assertEquals(100L, TenantContext.getTenantId());
    }

    @Test
    @DisplayName("无效Token应抛出TOKEN_INVALID异常")
    void preHandle_invalidToken() {
        when(request.getRequestURI()).thenReturn("/api/v1/members/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        BusinessException e = assertThrows(BusinessException.class,
                () -> authInterceptor.preHandle(request, response, null));
        assertEquals(ErrorCode.TOKEN_INVALID.getCode(), e.getCode());
    }

    @Test
    @DisplayName("afterCompletion应清除上下文")
    void afterCompletion_shouldClearContext() throws Exception {
        // 先设置上下文
        UserContext.set(new UserContext.UserInfo(1L, "test", "admin", 1L));
        TenantContext.set(new TenantContext.TenantInfo(1L, 1L, null));

        authInterceptor.afterCompletion(request, response, null, null);

        assertNull(UserContext.get());
        assertNull(TenantContext.get());
    }

    @Test
    @DisplayName("Authorization Header不以Bearer开头应抛出UNAUTHORIZED")
    void preHandle_nonBearerAuth() {
        when(request.getRequestURI()).thenReturn("/api/v1/members/list");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        BusinessException e = assertThrows(BusinessException.class,
                () -> authInterceptor.preHandle(request, response, null));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("空白名单时非白名单路径需要Token")
    void preHandle_emptyIgnoreUrls() {
        AuthInterceptor interceptor = new AuthInterceptor(jwtUtil, Collections.emptyList());
        when(request.getRequestURI()).thenReturn("/api/v1/members/list");
        when(request.getHeader("Authorization")).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, null));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), e.getCode());
    }
}
