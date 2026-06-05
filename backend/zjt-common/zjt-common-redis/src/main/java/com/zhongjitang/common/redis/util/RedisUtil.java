package com.zhongjitang.common.redis.util;

import com.zhongjitang.common.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * <p>
 * 封装Redis常用操作，所有Key自动添加租户前缀：{tenant_id}:key，
 * 实现多租户数据隔离。当租户上下文中无租户ID时，使用"global"作为前缀。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 全局租户前缀（无租户上下文时使用） */
    private static final String GLOBAL_TENANT = "global";

    /**
     * 设置缓存
     *
     * @param key   缓存Key
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(buildKey(key), value);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存Key
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    /**
     * 删除缓存
     *
     * @param key 缓存Key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(buildKey(key));
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存Key
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(buildKey(key), timeout, unit);
    }

    /**
     * 判断Key是否存在
     *
     * @param key 缓存Key
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(buildKey(key));
    }

    /**
     * 设置带过期时间的缓存
     *
     * @param key     缓存Key
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void setWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(buildKey(key), value, timeout, unit);
    }

    /**
     * 如果key不存在则设置（分布式锁常用）
     *
     * @param key     缓存Key
     * @param value   缓存值
     * @param timeout 过期时间(秒)
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(buildKey(key), value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 自增
     *
     * @param key 缓存Key
     * @return 自增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(buildKey(key));
    }

    /**
     * 自增指定步长
     *
     * @param key   缓存Key
     * @param delta 步长
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(buildKey(key), delta);
    }

    /**
     * 自减
     *
     * @param key 缓存Key
     * @return 自减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(buildKey(key));
    }

    /**
     * 自减指定步长
     *
     * @param key   缓存Key
     * @param delta 步长
     * @return 自减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(buildKey(key), delta);
    }

    /**
     * 构建带租户前缀的Key
     * <p>
     * 格式: {tenant_id}:key，无租户上下文时使用"global"作为前缀。
     * </p>
     *
     * @param key 原始Key
     * @return 带租户前缀的Key
     */
    private String buildKey(String key) {
        Long tenantId = TenantContext.getTenantId();
        String prefix = tenantId != null ? String.valueOf(tenantId) : GLOBAL_TENANT;
        return prefix + ":" + key;
    }
}
