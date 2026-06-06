package com.zhongjitang.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // 通过反射设置私有字段
        setField(jwtUtil, "secret", "zhongjitang-default-secret-key-must-be-at-least-256-bits-long-for-hs256");
        setField(jwtUtil, "expireMinutes", 120L);
        // 手动调用init方法初始化密钥
        jwtUtil.init();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("生成Token应返回非空字符串")
    void 生成Token应返回非空() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT三段式结构
    }

    @Test
    @DisplayName("解析Token应正确返回Claims")
    void 解析Token应正确返回Claims() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("张三", claims.get("username", String.class));
        assertEquals("member", claims.get("userType", String.class));
    }

    @Test
    @DisplayName("从Token中获取用户ID应正确")
    void 从Token获取用户ID() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        Long userId = jwtUtil.getUserId(token);

        assertNotNull(userId);
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("从Token中获取用户名应正确")
    void 从Token获取用户名() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        String username = jwtUtil.getUsername(token);

        assertEquals("张三", username);
    }

    @Test
    @DisplayName("从Token中获取用户类型应正确")
    void 从Token获取用户类型() {
        String token = jwtUtil.generateToken(1L, "admin", "employee", 100L, 1L);

        String userType = jwtUtil.getUserType(token);

        assertEquals("employee", userType);
    }

    @Test
    @DisplayName("从Token中获取租户ID应正确")
    void 从Token获取租户ID() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        Long tenantId = jwtUtil.getTenantId(token);

        assertNotNull(tenantId);
        assertEquals(100L, tenantId);
    }

    @Test
    @DisplayName("从Token中获取门店ID应正确")
    void 从Token获取门店ID() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        Long storeId = jwtUtil.getStoreId(token);

        assertNotNull(storeId);
        assertEquals(1L, storeId);
    }

    @Test
    @DisplayName("未过期的Token判断应返回false")
    void 未过期Token应返回false() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("过期的Token判断应返回true")
    void 过期Token应返回true() throws Exception {
        // 设置过期时间为0分钟，使Token立即过期
        setField(jwtUtil, "expireMinutes", 0L);
        jwtUtil.init();

        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        // 等待1秒确保过期
        Thread.sleep(1000);
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("无效Token解析应抛出JwtException")
    void 无效Token解析应抛异常() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(JwtException.class, () -> {
            jwtUtil.parseToken(invalidToken);
        });
    }

    @Test
    @DisplayName("刷新Token应生成新的有效Token")
    void 刷新Token应生成新Token() {
        String originalToken = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        String refreshedToken = jwtUtil.refreshToken(originalToken);

        assertNotNull(refreshedToken);
        assertNotEquals(originalToken, refreshedToken);
        assertEquals(1L, jwtUtil.getUserId(refreshedToken));
        assertEquals("张三", jwtUtil.getUsername(refreshedToken));
    }

    @Test
    @DisplayName("获取Token过期时间应返回未来时间")
    void 获取Token过期时间应为未来() {
        String token = jwtUtil.generateToken(1L, "张三", "member", 100L, 1L);

        LocalDateTime expireAt = jwtUtil.getExpireAt(token);

        assertNotNull(expireAt);
        assertTrue(expireAt.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("租户ID和门店ID为null时应正确处理")
    void 租户ID和门店ID为null() {
        String token = jwtUtil.generateToken(1L, "张三", "member", null, null);

        assertNull(jwtUtil.getTenantId(token));
        assertNull(jwtUtil.getStoreId(token));
        assertEquals(1L, jwtUtil.getUserId(token));
        assertEquals("张三", jwtUtil.getUsername(token));
    }
}
