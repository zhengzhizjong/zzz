package com.zhongjitang.common.redis.util;

import com.zhongjitang.common.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * <p>
 * 基于Redisson实现分布式锁，支持自动加锁/释放锁、重试机制。
 * Key格式: lock:{tenant_id}:{key}，实现多租户锁隔离。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtil {

    private final RedissonClient redissonClient;

    /** 锁Key前缀 */
    private static final String LOCK_PREFIX = "lock:";
    /** 全局租户前缀（无租户上下文时使用） */
    private static final String GLOBAL_TENANT = "global";

    /**
     * 尝试获取分布式锁
     *
     * @param key             锁Key
     * @param expireSeconds   锁过期时间（秒）
     * @param retryTimes      重试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     * @return true=获取成功, false=获取失败
     */
    public boolean lock(String key, long expireSeconds, int retryTimes, long retryIntervalMs) {
        RLock rLock = redissonClient.getLock(buildLockKey(key));
        try {
            return rLock.tryLock(retryIntervalMs * retryTimes, expireSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取锁被中断: key={}", key, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param key 锁Key
     */
    public void unlock(String key) {
        RLock rLock = redissonClient.getLock(buildLockKey(key));
        if (rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

    /**
     * 带返回值的分布式锁操作
     * <p>
     * 自动加锁和释放锁，在锁内执行业务逻辑并返回结果。
     * </p>
     *
     * @param key             锁Key
     * @param expireSeconds   锁过期时间（秒）
     * @param retryTimes      重试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     * @param supplier        业务逻辑
     * @param <T>             返回值类型
     * @return 业务逻辑执行结果
     */
    public <T> T lockWithResult(String key, long expireSeconds, int retryTimes,
                                long retryIntervalMs, Supplier<T> supplier) {
        boolean locked = false;
        try {
            locked = lock(key, expireSeconds, retryTimes, retryIntervalMs);
            if (!locked) {
                log.warn("获取锁失败: key={}", key);
                return null;
            }
            return supplier.get();
        } finally {
            if (locked) {
                unlock(key);
            }
        }
    }

    /**
     * 构建锁Key
     * <p>
     * 格式: lock:{tenant_id}:{key}，无租户上下文时使用"global"。
     * </p>
     *
     * @param key 原始Key
     * @return 带租户前缀的锁Key
     */
    private String buildLockKey(String key) {
        Long tenantId = TenantContext.getTenantId();
        String prefix = tenantId != null ? String.valueOf(tenantId) : GLOBAL_TENANT;
        return LOCK_PREFIX + prefix + ":" + key;
    }
}
