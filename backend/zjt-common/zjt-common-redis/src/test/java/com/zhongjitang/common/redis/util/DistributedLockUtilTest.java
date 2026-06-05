package com.zhongjitang.common.redis.util;

import com.zhongjitang.common.core.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁工具类测试
 */
class DistributedLockUtilTest {

    private DistributedLockUtil distributedLockUtil;
    private RedissonClient redissonClient;
    private RLock rLock;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        distributedLockUtil = new DistributedLockUtil(redissonClient);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("lock - 获取锁成功应返回true")
    void lock_success() throws Exception {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);

        boolean result = distributedLockUtil.lock("testKey", 30, 3, 100);
        assertTrue(result);
    }

    @Test
    @DisplayName("lock - 获取锁失败应返回false")
    void lock_fail() throws Exception {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

        boolean result = distributedLockUtil.lock("testKey", 30, 3, 100);
        assertFalse(result);
    }

    @Test
    @DisplayName("unlock - 当前线程持有锁时应释放")
    void unlock_heldByCurrentThread() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        distributedLockUtil.unlock("testKey");
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("unlock - 非当前线程持有锁时不应释放")
    void unlock_notHeldByCurrentThread() {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        distributedLockUtil.unlock("testKey");
        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("lockWithResult - 获取锁成功应执行业务逻辑并返回结果")
    void lockWithResult_success() throws Exception {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        String result = distributedLockUtil.lockWithResult("testKey", 30, 3, 100,
                () -> "success");
        assertEquals("success", result);
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("lockWithResult - 获取锁失败应返回null")
    void lockWithResult_lockFail() throws Exception {
        TenantContext.set(new TenantContext.TenantInfo(1L, null, null));
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

        String result = distributedLockUtil.lockWithResult("testKey", 30, 3, 100,
                () -> "success");
        assertNull(result);
    }

    @Test
    @DisplayName("无租户上下文时 - 锁Key应使用global前缀")
    void noTenantContext_shouldUseGlobalPrefix() {
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        distributedLockUtil.unlock("testKey");
        verify(redissonClient).getLock(eq("lock:global:testKey"));
    }
}
