package com.zhongjitang.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT工具类
 * <p>
 * 提供Token的生成、解析、刷新等功能，
 * 支持用户ID、用户名、用户类型、租户ID、门店ID等信息的嵌入。
 * </p>
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT签名密钥，从配置文件读取 */
    @Value("${jwt.secret:zhongjitang-default-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    /** Token过期时间（分钟），默认120分钟 */
    @Value("${jwt.expire-minutes:120}")
    private long expireMinutes;

    /** 签名密钥对象 */
    private SecretKey key;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型（member/employee/admin）
     * @param tenantId 租户ID
     * @param storeId  门店ID
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, String userType, Long tenantId, Long storeId) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expireMinutes * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("userType", userType)
                .claim("tenantId", tenantId)
                .claim("storeId", storeId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token字符串
     * @return Claims对象
     * @throws ExpiredJwtException Token已过期
     * @throws io.jsonwebtoken.JwtException Token无效
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取用户类型
     *
     * @param token JWT Token字符串
     * @return 用户类型
     */
    public String getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType", String.class);
    }

    /**
     * 从Token中获取租户ID
     *
     * @param token JWT Token字符串
     * @return 租户ID
     */
    public Long getTenantId(String token) {
        Claims claims = parseToken(token);
        Object tenantId = claims.get("tenantId");
        if (tenantId == null) {
            return null;
        }
        if (tenantId instanceof Integer) {
            return ((Integer) tenantId).longValue();
        }
        return (Long) tenantId;
    }

    /**
     * 从Token中获取门店ID
     *
     * @param token JWT Token字符串
     * @return 门店ID
     */
    public Long getStoreId(String token) {
        Claims claims = parseToken(token);
        Object storeId = claims.get("storeId");
        if (storeId == null) {
            return null;
        }
        if (storeId instanceof Integer) {
            return ((Integer) storeId).longValue();
        }
        return (Long) storeId;
    }

    /**
     * 判断Token是否已过期
     *
     * @param token JWT Token字符串
     * @return true=已过期, false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 刷新Token（在Token仍有效时生成新Token）
     *
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String userType = claims.get("userType", String.class);
        Object tenantIdObj = claims.get("tenantId");
        Object storeIdObj = claims.get("storeId");
        Long tenantId = tenantIdObj != null ? ((Number) tenantIdObj).longValue() : null;
        Long storeId = storeIdObj != null ? ((Number) storeIdObj).longValue() : null;

        return generateToken(userId, username, userType, tenantId, storeId);
    }

    /**
     * 获取Token过期时间（LocalDateTime）
     *
     * @param token JWT Token字符串
     * @return 过期时间
     */
    public LocalDateTime getExpireAt(String token) {
        Claims claims = parseToken(token);
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }
}
