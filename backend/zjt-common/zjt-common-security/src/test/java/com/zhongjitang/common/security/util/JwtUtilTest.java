package com.zhongjitang.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "zhongjitang-default-secret-key-must-be-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(jwtUtil, "expireMinutes", 120L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("生成Token - 应返回非空字符串")
    void generateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin", 100L, 200L);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("解析Token - 应正确获取所有声明")
    void parseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin", 100L, 200L);
        Claims claims = jwtUtil.parseToken(token);

        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("admin", claims.get("userType", String.class));
        assertNotNull(claims.get("tenantId"));
        assertNotNull(claims.get("storeId"));
    }

    @Test
    @DisplayName("getUserId - 应正确获取用户ID")
    void getUserId() {
        String token = jwtUtil.generateToken(123L, "testuser", "member", 10L, 20L);
        Long userId = jwtUtil.getUserId(token);
        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("getUsername - 应正确获取用户名")
    void getUsername() {
        String token = jwtUtil.generateToken(1L, "testuser", "member", 10L, 20L);
        String username = jwtUtil.getUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("getUserType - 应正确获取用户类型")
    void getUserType() {
        String token = jwtUtil.generateToken(1L, "testuser", "employee", 10L, 20L);
        String userType = jwtUtil.getUserType(token);
        assertEquals("employee", userType);
    }

    @Test
    @DisplayName("getTenantId - 应正确获取租户ID")
    void getTenantId() {
        String token = jwtUtil.generateToken(1L, "testuser", "admin", 999L, 200L);
        Long tenantId = jwtUtil.getTenantId(token);
        assertEquals(999L, tenantId);
    }

    @Test
    @DisplayName("getStoreId - 应正确获取门店ID")
    void getStoreId() {
        String token = jwtUtil.generateToken(1L, "testuser", "admin", 100L, 888L);
        Long storeId = jwtUtil.getStoreId(token);
        assertEquals(888L, storeId);
    }

    @Test
    @DisplayName("isTokenExpired - 未过期的Token应返回false")
    void isTokenExpired_false() {
        String token = jwtUtil.generateToken(1L, "testuser", "admin", 100L, 200L);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenExpired - 无效Token应返回true")
    void isTokenExpired_invalidToken() {
        assertTrue(jwtUtil.isTokenExpired("invalid.token.here"));
    }

    @Test
    @DisplayName("refreshToken - 应生成新的有效Token")
    void refreshToken() {
        String originalToken = jwtUtil.generateToken(1L, "testuser", "admin", 100L, 200L);
        String refreshedToken = jwtUtil.refreshToken(originalToken);

        assertNotNull(refreshedToken);
        assertNotEquals(originalToken, refreshedToken);

        // 新Token应包含相同的信息
        assertEquals(1L, jwtUtil.getUserId(refreshedToken));
        assertEquals("testuser", jwtUtil.getUsername(refreshedToken));
        assertEquals("admin", jwtUtil.getUserType(refreshedToken));
    }

    @Test
    @DisplayName("getExpireAt - 应返回未来的过期时间")
    void getExpireAt() {
        String token = jwtUtil.generateToken(1L, "testuser", "admin", 100L, 200L);
        LocalDateTime expireAt = jwtUtil.getExpireAt(token);
        assertNotNull(expireAt);
        assertTrue(expireAt.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("parseToken - 无效Token应抛出JwtException")
    void parseToken_invalid() {
        assertThrows(JwtException.class, () -> jwtUtil.parseToken("invalid.token.string"));
    }

    @Test
    @DisplayName("生成Token - storeId为null时应正常工作")
    void generateToken_nullStoreId() {
        String token = jwtUtil.generateToken(1L, "admin", "admin", 100L, null);
        Long storeId = jwtUtil.getStoreId(token);
        assertNull(storeId);
    }
}
