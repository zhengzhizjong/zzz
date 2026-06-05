package com.zhongjitang.common.redis.util;

import com.zhongjitang.common.core.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis工具类测试
 */
class RedisUtilTest {

    private RedisUtil redisUtil;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisUtil = new RedisUtil(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("set - 应调用RedisTemplate设置值")
    void set() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        redisUtil.set("testKey", "testValue");
        verify(valueOperations).set(eq("1:testKey"), eq("testValue"));
    }

    @Test
    @DisplayName("get - 应调用RedisTemplate获取值")
    void get() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(valueOperations.get("1:testKey")).thenReturn("testValue");
        Object result = redisUtil.get("testKey");
        assertEquals("testValue", result);
    }

    @Test
    @DisplayName("delete - 应调用RedisTemplate删除Key")
    void delete() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(redisTemplate.delete("1:testKey")).thenReturn(true);
        Boolean result = redisUtil.delete("testKey");
        assertTrue(result);
    }

    @Test
    @DisplayName("expire - 应设置过期时间")
    void expire() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(redisTemplate.expire("1:testKey", 60, TimeUnit.SECONDS)).thenReturn(true);
        Boolean result = redisUtil.expire("testKey", 60, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    @DisplayName("hasKey - 应判断Key是否存在")
    void hasKey() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(redisTemplate.hasKey("1:testKey")).thenReturn(true);
        Boolean result = redisUtil.hasKey("testKey");
        assertTrue(result);
    }

    @Test
    @DisplayName("setWithExpire - 应设置带过期时间的值")
    void setWithExpire() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        redisUtil.setWithExpire("testKey", "testValue", 60, TimeUnit.SECONDS);
        verify(valueOperations).set(eq("1:testKey"), eq("testValue"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("increment - 应自增")
    void increment() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(valueOperations.increment("1:testKey")).thenReturn(1L);
        Long result = redisUtil.increment("testKey");
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("decrement - 应自减")
    void decrement() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(valueOperations.decrement("1:testKey")).thenReturn(-1L);
        Long result = redisUtil.decrement("testKey");
        assertEquals(-1L, result);
    }

    @Test
    @DisplayName("无租户上下文时 - Key应使用global前缀")
    void noTenantContext_shouldUseGlobalPrefix() {
        redisUtil.set("testKey", "testValue");
        verify(valueOperations).set(eq("global:testKey"), eq("testValue"));
    }
}
