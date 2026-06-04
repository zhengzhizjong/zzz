package com.zhongjitang.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * 编码规则：
 * - 0: 成功
 * - 400xx: 客户端错误
 * - 401xx: 认证错误
 * - 403xx: 权限错误
 * - 404xx: 资源不存在
 * - 409xx: 资源冲突
 * - 429xx: 限流
 * - 500xx: 服务端错误
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 成功 ==========
    SUCCESS(0, "success"),

    // ========== 客户端错误 400xx ==========
    PARAM_ERROR(40001, "参数错误"),
    PARAM_MISSING(40002, "缺少必填参数"),
    PARAM_FORMAT_ERROR(40003, "参数格式错误"),

    // ========== 认证错误 401xx ==========
    UNAUTHORIZED(40101, "未认证，请先登录"),
    TOKEN_EXPIRED(40102, "Token已过期，请重新登录"),
    TOKEN_INVALID(40103, "Token无效"),
    LOGIN_FAILED(40104, "登录失败，手机号或密码错误"),

    // ========== 权限错误 403xx ==========
    FORBIDDEN(40301, "无权限访问"),
    FEATURE_NOT_ENABLED(40302, "套餐功能未开通"),
    TENANT_EXPIRED(40303, "租户已过期"),

    // ========== 资源不存在 404xx ==========
    NOT_FOUND(40401, "资源不存在"),
    STORE_NOT_FOUND(40402, "门店不存在"),
    TECHNICIAN_NOT_FOUND(40403, "技师不存在"),
    MEMBER_NOT_FOUND(40404, "会员不存在"),
    APPOINTMENT_NOT_FOUND(40405, "预约不存在"),
    SERVICE_ITEM_NOT_FOUND(40406, "服务项目不存在"),

    // ========== 资源冲突 409xx ==========
    CONFLICT(40901, "资源冲突"),
    DUPLICATE_APPOINTMENT(40902, "您已有该时段的预约"),
    SLOT_LOCKED(40903, "时段已被占用，请重新选择"),
    SLOT_TEMP_LOCKED(40904, "时段临时锁定中，请稍后重试"),
    APPOINTMENT_MODIFY_LIMIT(40905, "预约最多修改2次"),
    APPOINTMENT_CANCEL_TIMEOUT(40906, "距预约时间不足2小时，不可取消"),
    DUPLICATE_PHONE(40907, "手机号已注册"),

    // ========== 限流 429xx ==========
    RATE_LIMITED(42901, "请求过于频繁，请稍后重试"),
    APPOINTMENT_LIMIT(42902, "同一门店每天最多3个有效预约"),

    // ========== 服务端错误 500xx ==========
    INTERNAL_ERROR(50001, "系统内部错误"),
    LOCK_ACQUIRE_FAILED(50002, "获取锁失败，请重试"),
    LOCK_TIMEOUT(50003, "操作超时，请重试"),
    AI_SERVICE_ERROR(50004, "AI服务异常"),
    PAYMENT_ERROR(50005, "支付服务异常");

    private final Integer code;
    private final String message;
}
